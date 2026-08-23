package org.runnect.server.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.runnect.server.auth.dto.request.SignInRequestDto;
import org.runnect.server.auth.dto.response.SocialInfoResponseDto;
import org.runnect.server.config.jwt.JwtService;
import org.runnect.server.user.entity.SocialType;
import org.runnect.server.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * AuthService.signIn()은 "(email, provider)로 가입 여부 확인 -> 없으면 신규 저장" 순서로 동작한다.
 * 같은 소셜 계정으로 첫 로그인 요청이 동시에(더블탭, 타임아웃 후 재시도 등) 들어오면 둘 다
 * "미가입"으로 보고 각자 저장을 시도할 수 있다. (email, provider) 유니크 제약이 있어 DB가 하나는
 * 거부하는데(RunnectUser는 IDENTITY 전략이라 save() 호출 시점에 즉시 INSERT가 나가 그 자리에서
 * DataIntegrityViolationException이 던져진다), 수정 전에는 이 예외를 그대로 두어 실제로는
 * 가입에 성공한 요청까지 500으로 실패했다.
 *
 * 이 레이스는 스크랩 레이스와 성격이 다르다 — 두 요청 모두 "같은 계정으로 로그인 성공"이라는
 * 동일한 결과를 원하므로, 한쪽에 409를 주는 대신 실패한 저장 시도만 조용히 무시하고 둘 다
 * 로그인에 성공시키는 것이 맞다. Postgres는 트랜잭션 안에서 한 번 실패한 문장이 있으면 그 뒤
 * 모든 명령을 거부하므로, 저장 시도를 signIn()과 분리된 트랜잭션(REQUIRES_NEW)에서 실행해야
 * 실패해도 뒤이은 조회가 정상 동작한다 — 이 테스트는 수정 후 그 동작을 검증한다.
 */
@SpringBootTest
class AuthServiceSignInConcurrencyTest {

    private static final String TEST_EMAIL = "concurrency-signin-race@runnect.test";

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private KakaoSignInService kakaoSignInService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @PersistenceContext
    private EntityManager entityManager;

    @AfterEach
    void tearDown() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.executeWithoutResult(status ->
            entityManager.createQuery("DELETE FROM RunnectUser u WHERE u.email = :email")
                .setParameter("email", TEST_EMAIL)
                .executeUpdate());
    }

    @Test
    void 동시에_같은_소셜계정으로_첫_로그인하면_둘_다_로그인에_성공한다() throws InterruptedException {
        when(kakaoSignInService.getSocialInfo("race-token"))
            .thenReturn(SocialInfoResponseDto.of(TEST_EMAIL, "race-social-id"));
        when(jwtService.issuedAccessToken(any())).thenReturn("access-token");
        when(jwtService.issuedRefreshToken(any())).thenReturn("refresh-token");

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicReference<Throwable> capturedException = new AtomicReference<>();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    authService.signIn(new SignInRequestDto("race-token", "KAKAO"));
                    successCount.incrementAndGet();
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
        assertThat(capturedException.get())
            .withFailMessage(
                "수정 전에는 DataIntegrityViolationException이 그대로 새서 한쪽 로그인이 실패했음. 실제: %s",
                capturedException.get()
            )
            .isNull();
        assertThat(successCount.get())
            .withFailMessage("두 로그인 요청 모두 성공해야 한다")
            .isEqualTo(2);
        assertThat(userRepository.findByEmailAndProvider(TEST_EMAIL, SocialType.KAKAO)).isPresent();
    }
}
