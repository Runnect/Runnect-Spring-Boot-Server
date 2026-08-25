package org.runnect.server.markerquota;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.runnect.server.common.exception.ConflictException;
import org.runnect.server.markerquota.dto.response.GrantMarkerRewardResponseDto;
import org.runnect.server.markerquota.service.MarkerQuotaService;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.entity.SocialType;
import org.runnect.server.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 마커 재화의 두 가지 갱신 경로(소비/적립)가 동시 요청에도 정합성을 지키는지 실제 로컬
 * Postgres로 검증한다.
 *
 * 1) {@link ConsumeQuota#요청이_몰려도_잔액이_음수로_내려가지_않는다}: 소비는 잔액 확인과 차감을
 *    UserRepository.consumeMarkerQuota() 조건부 UPDATE 한 문장으로 처리한다. 트랜잭션/락을
 *    직접 관리하지 않아도, 이 문장 자체가 DB에서 원자적으로 실행되므로 잔액보다 많은 소비가
 *    성공할 수 없다는 것을 검증한다.
 * 2) {@link GrantReward#같은_트랜잭션ID로_동시에_요청해도_한_번만_지급된다}: 적립은
 *    MarkerRewardClaim의 (user_id, rewardTransactionId) 유니크 제약으로 멱등성을 보장한다.
 *    같은 광고 시청 건에 대한 지급 요청이 재시도 등으로 동시에 여러 번 들어와도 실제 지급은
 *    정확히 한 번만 일어나야 한다.
 */
@SpringBootTest
class MarkerQuotaConcurrencyTest {

    @Autowired
    private MarkerQuotaService markerQuotaService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

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
            // 리워드 지급 과정에서 생긴 MarkerRewardClaim 자식 row부터 지워야 FK 제약을 안 건드린다.
            entityManager.createQuery("DELETE FROM MarkerRewardClaim c WHERE c.runnectUser.id = :userId")
                .setParameter("userId", testUserId)
                .executeUpdate();
            userRepository.deleteById(testUserId);
        });
    }

    private Long createTestUserWithBalance(String nickname, int balance) {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        Long userId = tx.execute(status -> userRepository.save(
            RunnectUser.builder()
                .nickname(nickname)
                .socialId("marker-quota-test-social-id-" + nickname)
                .email("marker-quota-test-" + nickname + "@runnect.test")
                .provider(SocialType.VISITOR)
                .build()
        ).getId());

        tx.executeWithoutResult(status -> {
            int updated = userRepository.consumeMarkerQuota(userId, 20 - balance);
            assertThat(updated).withFailMessage("테스트 초기 잔액 세팅 실패").isEqualTo(1);
        });
        return userId;
    }

    @Nested
    @DisplayName("마커 재화 소비")
    class ConsumeQuota {

        @Test
        void 요청이_몰려도_잔액이_음수로_내려가지_않는다() throws InterruptedException {
            int initialBalance = 5;
            testUserId = createTestUserWithBalance("cc-consume", initialBalance);
            int threadCount = 10;

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch readyLatch = new CountDownLatch(threadCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger insufficientCount = new AtomicInteger(0);
            AtomicInteger unexpectedFailureCount = new AtomicInteger(0);

            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        readyLatch.countDown();
                        startLatch.await();
                        markerQuotaService.consume(testUserId, 1);
                        successCount.incrementAndGet();
                    } catch (ConflictException e) {
                        insufficientCount.incrementAndGet();
                    } catch (Exception e) {
                        unexpectedFailureCount.incrementAndGet();
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
            assertThat(unexpectedFailureCount.get())
                .withFailMessage("예상 못 한 예외가 %d건 발생함", unexpectedFailureCount.get())
                .isZero();
            assertThat(successCount.get())
                .withFailMessage("초기 잔액(%d)보다 많은 소비가 성공함", initialBalance)
                .isEqualTo(initialBalance);
            assertThat(insufficientCount.get()).isEqualTo(threadCount - initialBalance);

            int finalBalance = markerQuotaService.getBalance(testUserId);
            assertThat(finalBalance)
                .withFailMessage("잔액이 음수로 내려가거나 잘못 계산됨 (최종 잔액=%d)", finalBalance)
                .isZero();
        }
    }

    @Nested
    @DisplayName("마커 재화 적립(광고 리워드)")
    class GrantReward {

        @Test
        void 같은_트랜잭션ID로_동시에_요청해도_한_번만_지급된다() throws InterruptedException {
            testUserId = createTestUserWithBalance("cc-reward", 0);
            int threadCount = 10;
            int rewardAmount = 5;
            String sameRewardTransactionId = "ad-watch-txn-duplicate-test";

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch readyLatch = new CountDownLatch(threadCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);
            AtomicInteger grantedCount = new AtomicInteger(0);
            AtomicInteger rejectedCount = new AtomicInteger(0);
            AtomicInteger unexpectedFailureCount = new AtomicInteger(0);

            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        readyLatch.countDown();
                        startLatch.await();
                        GrantMarkerRewardResponseDto result = markerQuotaService.grantFromAd(
                            testUserId, rewardAmount, sameRewardTransactionId);
                        if (result.isGranted()) {
                            grantedCount.incrementAndGet();
                        } else {
                            rejectedCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        unexpectedFailureCount.incrementAndGet();
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
            assertThat(unexpectedFailureCount.get())
                .withFailMessage("예상 못 한 예외가 %d건 발생함", unexpectedFailureCount.get())
                .isZero();
            assertThat(grantedCount.get())
                .withFailMessage("같은 트랜잭션ID로 %d번 지급이 성공함 — 중복 지급 발생", grantedCount.get())
                .isEqualTo(1);
            assertThat(rejectedCount.get()).isEqualTo(threadCount - 1);

            int finalBalance = markerQuotaService.getBalance(testUserId);
            assertThat(finalBalance)
                .withFailMessage("리워드가 중복 반영되어 잔액이 %d이 아닌 %d로 기록됨", rewardAmount, finalBalance)
                .isEqualTo(rewardAmount);
        }
    }
}
