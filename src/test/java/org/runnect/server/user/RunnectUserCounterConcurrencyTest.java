package org.runnect.server.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.runnect.server.common.module.concurrency.OptimisticLockRetrier;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.entity.SocialType;
import org.runnect.server.user.entity.StampType;
import org.runnect.server.user.repository.UserRepository;
import org.runnect.server.user.service.UserStampService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * RunnectUser의 createdCourse 등 카운터 필드가 순수 in-memory ++ 로 구현되어 있어
 * Lost Update(갱신 유실)에 취약했던 문제를, 실제 로컬 Postgres에 대해 재현/검증한다.
 *
 * 1) {@link #버전_없이_직접_저장하면_충돌이_감지된다}: @Version을 추가하기 전에는 이 테스트가
 *    "두 번 증가시켰는데 실제로는 1로 기록됨"이라는 assertion 실패로 버그를 실증했다
 *    (커밋 로그 참고). @Version 추가 후에는 같은 재현 절차가 더 이상 조용히 데이터를
 *    잘못 기록하지 않고, ObjectOptimisticLockingFailureException을 던져 충돌을 명시적으로
 *    감지한다 — "조용한 데이터 손상"에서 "감지 가능한 실패"로 바뀐 것을 보여준다.
 * 2) {@link #동시에_여러_요청이_같은_유저의_카운터를_증가시켜도_유실되지_않는다}: 실제 프로덕션
 *    경로(OptimisticLockRetrier + UserStampService.recordActivityAndAwardStamp)를 여러
 *    스레드에서 동시에 호출해도, 충돌은 자동으로 재시도되어 최종 카운트가 정확히 맞고
 *    호출부(CourseService 등)에는 예외가 전파되지 않음을 검증한다.
 */
@SpringBootTest
class RunnectUserCounterConcurrencyTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private UserStampService userStampService;

    @Autowired
    private OptimisticLockRetrier optimisticLockRetrier;

    @PersistenceContext
    private EntityManager entityManager;

    private Long testUserId;

    @AfterEach
    void tearDown() {
        if (testUserId == null) {
            return;
        }
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.executeWithoutResult(status -> {
            // 카운터 증가 과정에서 함께 생긴 UserStamp 자식 row부터 지워야 FK 제약을 안 건드린다.
            entityManager.createQuery("DELETE FROM UserStamp s WHERE s.runnectUser.id = :userId")
                .setParameter("userId", testUserId)
                .executeUpdate();
            userRepository.deleteById(testUserId);
        });
    }

    private Long createTestUser(String nickname) {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        return tx.execute(status -> userRepository.save(
            RunnectUser.builder()
                .nickname(nickname)
                .socialId("concurrency-test-social-id-" + nickname)
                .email("concurrency-test-" + nickname + "@runnect.test")
                .provider(SocialType.VISITOR)
                .build()
        ).getId());
    }

    @Test
    void 버전_없이_직접_저장하면_충돌이_감지된다() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        testUserId = createTestUser("cc-lost-update");

        // 요청 A, 요청 B가 서로의 커밋 전 상태를 보지 못한 채 같은 초기값(createdCourse=0, version=0)을 읽는다.
        RunnectUser userSeenByRequestA = tx.execute(status -> userRepository.findById(testUserId).orElseThrow());
        RunnectUser userSeenByRequestB = tx.execute(status -> userRepository.findById(testUserId).orElseThrow());

        userSeenByRequestA.updateCreatedCourse();
        userSeenByRequestB.updateCreatedCourse();

        // 요청 A 커밋 -> createdCourse=1, version이 올라감
        tx.executeWithoutResult(status -> userRepository.save(userSeenByRequestA));

        // 요청 B는 자신이 읽은 시점(version=0)을 기준으로 저장을 시도한다.
        // @Version이 없다면 이 저장은 조용히 성공해 요청 A의 증가분을 덮어썼을 것이다(Lost Update).
        // @Version이 있으면 버전 불일치가 감지되어 예외가 발생한다 — 유실이 아니라 실패로 바뀐다.
        assertThatThrownBy(() -> tx.executeWithoutResult(status -> userRepository.save(userSeenByRequestB)))
            .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }

    @Test
    void 동시에_여러_요청이_같은_유저의_카운터를_증가시켜도_유실되지_않는다() throws InterruptedException {
        testUserId = createTestUser("cc-fixed-path");
        int threadCount = 10;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    // 실제 CourseService/RecordService/ScrapService가 호출하는 것과 동일한 경로.
                    optimisticLockRetrier.runWithRetry(
                        () -> userStampService.recordActivityAndAwardStamp(testUserId, StampType.c)
                    );
                } catch (Exception e) {
                    failureCount.incrementAndGet();
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
        assertThat(failureCount.get())
            .withFailMessage("재시도로도 해결되지 않은 충돌이 %d건 발생함", failureCount.get())
            .isZero();

        RunnectUser result = userRepository.findById(testUserId).orElseThrow();
        assertThat(result.getCreatedCourse())
            .withFailMessage(
                "%d번 동시에 증가시켰는데 실제로는 %d로 기록됨 — 여전히 Lost Update 발생",
                threadCount, result.getCreatedCourse()
            )
            .isEqualTo((long) threadCount);
    }
}
