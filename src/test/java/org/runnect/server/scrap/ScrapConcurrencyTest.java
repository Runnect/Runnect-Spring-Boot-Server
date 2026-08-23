package org.runnect.server.scrap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.runnect.server.publicCourse.entity.PublicCourse;
import org.runnect.server.publicCourse.repository.PublicCourseRepository;
import org.runnect.server.scrap.dto.request.CreateAndDeleteScrapRequestDto;
import org.runnect.server.scrap.service.ScrapService;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.entity.SocialType;
import org.runnect.server.user.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 스크랩 생성 로직(ScrapService.createAndDeleteScrap)이 "기존 스크랩 조회 → 없으면 새로 저장"
 * 순서로 동작하는데, 이 사이에 동시 요청(더블탭, 다중 기기)이 끼어들면 둘 다 "스크랩 없음"을
 * 보고 각자 INSERT를 시도할 수 있다. (user_id, public_course_id) 유니크 제약이 있어 DB가
 * 하나는 거부하는데, 수정 전에는 이 예외를 그대로 두어 500으로 샜다(실제 로컬 Postgres에
 * 대해 재현해 org.springframework.dao.DataIntegrityViolationException이 그대로 전파됨을
 * 확인한 뒤 커밋 로그에 남김). 수정 후에는 HealthService의 기존 처리 패턴과 동일하게
 * ConflictException(409)으로 변환된다 — 이 테스트는 그 수정 후 동작을 검증한다.
 *
 * PublicCourseRepository는 @MockBean으로 대체한다 — 이 프로젝트의 로컬 Postgres/PostGIS
 * JDBC 드라이버 조합이 geometry(Course.path) 컬럼을 포함한 JOIN FETCH 결과를 추출할 때
 * 알려진 결함이 있어(다른 통합 테스트에서도 동일 사유로 우회한 이력 있음), 이 테스트가
 * 검증하려는 "스크랩 유니크 제약 경합"과 무관한 그 문제를 피하기 위함이다. Scrap/User
 * 리포지토리와 트랜잭션은 모두 실제 로컬 Postgres를 그대로 사용한다.
 */
@SpringBootTest
class ScrapConcurrencyTest {

    private static final Long EXISTING_PUBLIC_COURSE_ID = 1L;

    @Autowired
    private ScrapService scrapService;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private PublicCourseRepository publicCourseRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @PersistenceContext
    private EntityManager entityManager;

    private Long testUserId;

    @BeforeEach
    void setUpPublicCourseStub() {
        PublicCourse publicCourse = PublicCourse.builder()
            .title("스텁 공개 코스")
            .description("동시성 테스트용 스텁")
            .build();
        ReflectionTestUtils.setField(publicCourse, "id", EXISTING_PUBLIC_COURSE_ID);
        when(publicCourseRepository.findById(EXISTING_PUBLIC_COURSE_ID)).thenReturn(Optional.of(publicCourse));
    }

    @AfterEach
    void tearDown() {
        if (testUserId == null) {
            return;
        }
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.executeWithoutResult(status -> {
            entityManager.createQuery("DELETE FROM Scrap s WHERE s.runnectUser.id = :userId")
                .setParameter("userId", testUserId)
                .executeUpdate();
            entityManager.createQuery("DELETE FROM UserStamp s WHERE s.runnectUser.id = :userId")
                .setParameter("userId", testUserId)
                .executeUpdate();
            userRepository.deleteById(testUserId);
        });
    }

    private CreateAndDeleteScrapRequestDto scrapRequest(Long publicCourseId, boolean scrapTF) {
        CreateAndDeleteScrapRequestDto dto = BeanUtils.instantiateClass(CreateAndDeleteScrapRequestDto.class);
        ReflectionTestUtils.setField(dto, "publicCourseId", publicCourseId);
        ReflectionTestUtils.setField(dto, "scrapTF", scrapTF);
        return dto;
    }

    @Test
    void 동시에_같은_코스를_스크랩하면_한쪽은_ConflictException으로_처리된다() throws InterruptedException {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        testUserId = tx.execute(status -> userRepository.save(
            RunnectUser.builder()
                .nickname("cc-scrap-race")
                .socialId("concurrency-test-social-id-scrap")
                .email("concurrency-test-scrap@runnect.test")
                .provider(SocialType.VISITOR)
                .build()
        ).getId());

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicReference<Throwable> capturedException = new AtomicReference<>();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    scrapService.createAndDeleteScrap(testUserId, scrapRequest(EXISTING_PUBLIC_COURSE_ID, true));
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
            .withFailMessage("동시 스크랩 요청 중 하나가 실패할 것으로 예상했지만 둘 다 성공함")
            .isNotNull();
        assertThat(exception)
            .withFailMessage(
                "수정 전에는 DataIntegrityViolationException이 그대로 샜음. 수정 후 예상: ConflictException(409). 실제: %s",
                exception
            )
            .isInstanceOf(org.runnect.server.common.exception.ConflictException.class);
    }
}
