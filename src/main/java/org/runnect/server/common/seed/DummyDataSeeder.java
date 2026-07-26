package org.runnect.server.common.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.PrecisionModel;
import org.runnect.server.course.entity.Course;
import org.runnect.server.course.repository.CourseRepository;
import org.runnect.server.publicCourse.entity.PublicCourse;
import org.runnect.server.publicCourse.repository.PublicCourseRepository;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.entity.SocialType;
import org.runnect.server.user.repository.UserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// 성능 테스트용 더미 데이터 시더. runnect.seed-dummy-data=true 일 때만 동작하며,
// 이미 공개 코스가 있으면 아무 것도 하지 않는다. dev 전용, prod에는 이 프로퍼티를 절대 넣지 않는다.
// ApplicationReadyEvent 이후 지연 실행: 부팅 직후(가장 메모리가 예민한 구간)에 시딩 부하가
// 겹쳐서 배포가 반복적으로 죽는 문제가 있었음 — 앱이 완전히 안정된 뒤에 실행되도록 늦춤.
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "runnect.seed-dummy-data", havingValue = "true")
public class DummyDataSeeder {

    private static final String[] TITLES = {
        "한강 러닝코스", "올림픽공원 한바퀴", "반포 야경 코스", "여의도 벚꽃길", "서울숲 트레일",
        "남산 둘레길", "청계천 코스", "잠실 러닝", "안양천 코스", "탄천 코스"
    };

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final PublicCourseRepository publicCourseRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        Thread seedThread = new Thread(this::seed, "dummy-data-seeder");
        seedThread.setDaemon(true);
        seedThread.start();
    }

    private void seed() {
        try {
            Thread.sleep(30_000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        if (publicCourseRepository.countBy() > 0) {
            log.info("[DummyDataSeeder] 이미 공개 코스가 존재해 시딩을 건너뜁니다.");
            return;
        }

        log.info("[DummyDataSeeder] 성능 테스트용 더미 데이터 생성을 시작합니다.");

        RunnectUser user = userRepository.save(RunnectUser.builder()
            .nickname("perftest")
            .socialId("perftest-dummy-social-id")
            .email("perftest-dummy@runnect.test")
            .provider(SocialType.GOOGLE)
            .build());

        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

        for (int i = 0; i < TITLES.length; i++) {
            LineString path = geometryFactory.createLineString(new Coordinate[]{
                new Coordinate(37.50 + i * 0.001, 127.00 + i * 0.001),
                new Coordinate(37.51 + i * 0.001, 127.01 + i * 0.001),
                new Coordinate(37.52 + i * 0.001, 127.02 + i * 0.001)
            });

            Course course = courseRepository.save(Course.builder()
                .runnectUser(user)
                .title(TITLES[i])
                .departureRegion("서울")
                .departureCity("서울시")
                .departureTown("성동구")
                .departureDetail("성수동")
                .departureName("성수역")
                .distance(5.0f + i)
                .image("https://runnect-dummy.test/image.png")
                .path(path)
                .build());
            course.uploadCourse();

            publicCourseRepository.save(PublicCourse.builder()
                .course(course)
                .title(TITLES[i])
                .description("성능 테스트용 더미 데이터입니다.")
                .build());
        }

        log.info("[DummyDataSeeder] 더미 데이터 {}건 생성 완료.", TITLES.length);
    }
}
