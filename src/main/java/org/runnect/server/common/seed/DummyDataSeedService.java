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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 실제 더미 데이터 생성 로직. 하나의 트랜잭션으로 묶어서 중간에 실패하면 전부 롤백되도록 한다
// (유저만 저장되고 코스는 저장 안 된 상태로 남는 부분 실패를 방지 — 그 부분 실패가
// 재시작마다 유니크 제약 위반 크래시 루프의 원인이었음). 유저 존재 여부도 먼저 확인해서
// 재실행해도 안전하게(idempotent) 동작한다.
@Slf4j
@Service
@RequiredArgsConstructor
public class DummyDataSeedService {

    private static final String[] TITLES = {
        "한강 러닝코스", "올림픽공원 한바퀴", "반포 야경 코스", "여의도 벚꽃길", "서울숲 트레일",
        "남산 둘레길", "청계천 코스", "잠실 러닝", "안양천 코스", "탄천 코스"
    };
    private static final String SEED_EMAIL = "perftest-dummy@runnect.test";

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final PublicCourseRepository publicCourseRepository;

    @Transactional
    public void seedIfNeeded() {
        if (publicCourseRepository.countBy() > 0) {
            log.info("[DummyDataSeedService] 이미 공개 코스가 존재해 시딩을 건너뜁니다.");
            return;
        }

        RunnectUser user = userRepository.findByEmailAndProvider(SEED_EMAIL, SocialType.GOOGLE)
            .orElseGet(() -> userRepository.save(RunnectUser.builder()
                .nickname("perftest")
                .socialId("perftest-dummy-social-id")
                .email(SEED_EMAIL)
                .provider(SocialType.GOOGLE)
                .build()));

        log.info("[DummyDataSeedService] 성능 테스트용 더미 데이터 생성을 시작합니다.");

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

        log.info("[DummyDataSeedService] 더미 데이터 {}건 생성 완료.", TITLES.length);
    }
}
