package org.runnect.server.user.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.runnect.server.user.dto.request.UpdateUserNicknameRequestDto;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.entity.SocialType;
import org.runnect.server.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * updateUserNickname()은 "닉네임 중복 조회 -> 없으면 변경" 순서로 동작하는데, 서로 다른 두 유저가
 * 동시에 같은 새 닉네임으로 변경을 요청하면 둘 다 "중복 없음"을 보고 각자 변경을 시도할 수 있다.
 * nickname 컬럼의 unique 제약으로 DB가 하나는 거부하는데, 수정 전에는 엔티티 필드만 바꾸는
 * 더티 체킹이라 이 메서드 안에서 flush를 강제하지 않으면 위반 여부가 트랜잭션 커밋 시점에야
 * DataIntegrityViolationException으로 그대로 샜다(실제 로컬 Postgres에 대해 재현해 확인한 뒤
 * 커밋 로그에 남김). 수정 후에는 saveAndFlush로 메서드 안에서 직접 터뜨려 잡고
 * DuplicateNicknameException(409)으로 변환한다 — 이 테스트는 그 수정 후 동작을 검증한다.
 */
@SpringBootTest
class UserNicknameConcurrencyTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @PersistenceContext
    private EntityManager entityManager;

    private Long userId1;
    private Long userId2;

    @AfterEach
    void tearDown() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.executeWithoutResult(status -> {
            entityManager.createQuery("DELETE FROM UserStamp s WHERE s.runnectUser.id IN :ids")
                .setParameter("ids", java.util.List.of(userId1, userId2))
                .executeUpdate();
            if (userId1 != null) {
                userRepository.deleteById(userId1);
            }
            if (userId2 != null) {
                userRepository.deleteById(userId2);
            }
        });
    }

    @Test
    void 동시에_서로_다른_유저가_같은_닉네임으로_변경하면_한쪽만_성공한다() throws InterruptedException {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        userId1 = tx.execute(status -> userRepository.save(
            RunnectUser.builder()
                .nickname("cc-nick-race-1")
                .socialId("concurrency-test-social-id-nick-1")
                .email("concurrency-test-nick-1@runnect.test")
                .provider(SocialType.VISITOR)
                .build()
        ).getId());
        userId2 = tx.execute(status -> userRepository.save(
            RunnectUser.builder()
                .nickname("cc-nick-race-2")
                .socialId("concurrency-test-social-id-nick-2")
                .email("concurrency-test-nick-2@runnect.test")
                .provider(SocialType.VISITOR)
                .build()
        ).getId());

        String targetNickname = "레이스닉네임";
        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicReference<Throwable> capturedException = new AtomicReference<>();

        Long[] userIds = {userId1, userId2};
        for (int i = 0; i < threadCount; i++) {
            Long targetUserId = userIds[i];
            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    userService.updateUserNickname(targetUserId,
                        new UpdateUserNicknameRequestDto(targetNickname));
                } catch (Throwable e) {
                    capturedException.compareAndSet(null, e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        boolean completed = doneLatch.await(15, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(completed).withFailMessage("스레드가 제한 시간 내에 끝나지 않음").isTrue();

        Throwable exception = capturedException.get();
        assertThat(exception)
            .withFailMessage("동시 닉네임 변경 요청 중 하나가 실패할 것으로 예상했지만 둘 다 성공함")
            .isNotNull();
        assertThat(exception)
            .withFailMessage(
                "수정 전에는 DataIntegrityViolationException이 그대로 샜음. 수정 후 예상: DuplicateNicknameException(409). 실제: %s",
                exception
            )
            .isInstanceOf(org.runnect.server.user.exception.userException.DuplicateNicknameException.class);
    }
}
