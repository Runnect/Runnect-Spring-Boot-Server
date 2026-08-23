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
 * 그 다음 단계로, 스탬프/카운터 갱신(recordActivityAndAwardStamp)이 메인 저장 성공 여부와
 * 무관하게 반영되지 않는지도 함께 검증한다 — 실패한 시도까지 카운트되면 안 되기 때문이다.
 *
 * PublicCourseRepository는 @MockBean으로 대체한다 — 이 프로젝트의 로컬 Postgres/PostGIS
 * JDBC 드라이버 조합이 geometry(Course.path) 컬럼을 포함한 JOIN FETCH 결과를 추출할 때
 * 알려진 결함이 있어(다른 통합 테스트에서도 동일 사유로 우회한 이력 있음), 이 테스트가
 * 검증하려는 "스크랩 유니크 제약 경합"과 무관한 그 문제를 피하기 위함이다. Scrap/User
 * 리포지토리와 트랜잭션은 모두 실제 로컬 Postgres를 그대로 사용한다.
 *
 * 단, publicCourseRepository를 목으로 대체하는 것과 별개로 (user_id, public_course_id)
 * FK 제약을 만족하려면 실제 course/public_course row가 DB에 존재해야 한다 — 처음에는 로컬에
 * 우연히 남아있던 더미 데이터(id=1)를 가정하고 작성했다가, CI(매번 새로 뜨는 빈 DB)에서는
 * 그 row가 아예 없어 두 스레드 모두 "유니크 제약 위반"이 아니라 "FK 제약 위반"으로 실패해버려
 * 테스트가 실제로 검증하려는 레이스와 무관하게 우연히 같은 예외 타입(ConflictException)으로
 * 통과해버리는 것을 CI 재현으로 확인했다. 이제는 이 테스트가 스스로 필요한 course/public_course
 * row를 만들어 어떤 환경에서 실행해도 동일하게 동작하도록 한다.
 */
@SpringBootTest
class ScrapConcurrencyTest {

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
    private Long testCourseId;
    private Long testPublicCourseId;

    @BeforeEach
    void setUpPublicCourseStub() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.executeWithoutResult(status -> {
            testCourseId = ((Number) entityManager.createNativeQuery(
                    "INSERT INTO course "
                        + "(title, departure_region, departure_city, departure_town, distance, image, is_private, path, created_at, updated_at) "
                        + "VALUES ('동시성 테스트용 코스', '경기', '시흥시', '정왕동', 1.0, 'https://image.example/cc-scrap.png', "
                        + "false, ST_GeomFromText('LINESTRING(127.0 37.5, 127.1 37.6)', 4326), now(), now()) "
                        + "RETURNING id")
                .getSingleResult()).longValue();

            testPublicCourseId = ((Number) entityManager.createNativeQuery(
                    "INSERT INTO public_course (title, description, course_id, created_at, updated_at) "
                        + "VALUES ('스텁 공개 코스', '동시성 테스트용 스텁', " + testCourseId + ", now(), now()) "
                        + "RETURNING id")
                .getSingleResult()).longValue();
        });

        PublicCourse publicCourse = PublicCourse.builder()
            .title("스텁 공개 코스")
            .description("동시성 테스트용 스텁")
            .build();
        ReflectionTestUtils.setField(publicCourse, "id", testPublicCourseId);
        when(publicCourseRepository.findById(testPublicCourseId)).thenReturn(Optional.of(publicCourse));
    }

    @AfterEach
    void tearDown() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.executeWithoutResult(status -> {
            if (testUserId != null) {
                entityManager.createQuery("DELETE FROM Scrap s WHERE s.runnectUser.id = :userId")
                    .setParameter("userId", testUserId)
                    .executeUpdate();
                entityManager.createQuery("DELETE FROM UserStamp s WHERE s.runnectUser.id = :userId")
                    .setParameter("userId", testUserId)
                    .executeUpdate();
                userRepository.deleteById(testUserId);
            }
            if (testPublicCourseId != null) {
                entityManager.createNativeQuery("DELETE FROM public_course WHERE id = " + testPublicCourseId)
                    .executeUpdate();
            }
            if (testCourseId != null) {
                entityManager.createNativeQuery("DELETE FROM course WHERE id = " + testCourseId)
                    .executeUpdate();
            }
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
                    scrapService.createAndDeleteScrap(testUserId, scrapRequest(testPublicCourseId, true));
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

        // 스크랩은 실제로 1건만 만들어졌는데(한쪽은 ConflictException으로 실패), 스탬프/카운터
        // 업데이트가 저장 성공 여부와 무관하게 REQUIRES_NEW로 즉시 커밋돼버리면 두 번 다
        // 반영돼 createdScrap이 2가 될 수 있다 — 실패한 시도까지 카운트되는 것이므로 그 자체가 버그.
        RunnectUser user = userRepository.findById(testUserId).orElseThrow();
        assertThat(user.getCreatedScrap())
            .withFailMessage(
                "실제로 생성된 스크랩은 1건인데 카운터가 %d로 기록됨 — 실패한 시도의 스탬프/카운터 갱신이 "
                    + "메인 저장 실패와 무관하게 커밋돼버린 것",
                user.getCreatedScrap()
            )
            .isEqualTo(1L);
    }
}
