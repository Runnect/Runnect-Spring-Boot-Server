package org.runnect.server.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.LineString;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.runnect.server.common.exception.BadRequestException;
import org.runnect.server.common.exception.NotFoundException;
import org.runnect.server.common.module.concurrency.OptimisticLockRetrier;
import org.runnect.server.common.module.convert.CoordinateDto;
import org.runnect.server.common.module.convert.CoordinatePathConverter;
import org.runnect.server.course.dto.request.CourseCreateRequestDto;
import org.runnect.server.course.dto.response.CourseCreateResponseDto;
import org.runnect.server.course.dto.response.CourseGetByUserResponseDto;
import org.runnect.server.course.dto.response.DeleteCoursesResponseDto;
import org.runnect.server.course.dto.response.GetCourseDetailResponseDto;
import org.runnect.server.course.dto.response.UpdateCourseResponseDto;
import org.runnect.server.course.entity.Course;
import org.runnect.server.course.repository.CourseRepository;
import org.runnect.server.publicCourse.entity.PublicCourse;
import org.runnect.server.publicCourse.repository.PublicCourseRepository;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.entity.SocialType;
import org.runnect.server.user.entity.StampType;
import org.runnect.server.user.exception.userException.NotFoundUserException;
import org.runnect.server.user.repository.UserRepository;
import org.runnect.server.user.service.UserStampService;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private PublicCourseRepository publicCourseRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserStampService userStampService;
    @Mock
    private OptimisticLockRetrier optimisticLockRetrier;

    private CourseService courseService;

    @BeforeEach
    void setUp() {
        courseService = new CourseService(courseRepository, publicCourseRepository, userRepository,
            userStampService, optimisticLockRetrier);
        org.mockito.Mockito.lenient().doAnswer(invocation -> {
            Runnable action = invocation.getArgument(0);
            action.run();
            return null;
        }).when(optimisticLockRetrier).runWithRetry(any());
    }

    private RunnectUser buildUser(Long id) {
        RunnectUser user = RunnectUser.builder()
            .nickname("러너" + id)
            .socialId("social-" + id)
            .email("user" + id + "@runnect.io")
            .provider(SocialType.KAKAO)
            .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private List<CoordinateDto> validPath() {
        return Arrays.asList(
            new CoordinateDto(37.5665, 126.9780),
            new CoordinateDto(37.5651, 126.9895)
        );
    }

    private LineString validLineString() {
        return CoordinatePathConverter.coorConvertPath(validPath());
    }

    private CourseCreateRequestDto createRequestDto(List<CoordinateDto> path, String departureAddress) {
        return new CourseCreateRequestDto(path, "정왕역 코스", 5.2f, "정왕역", departureAddress);
    }

    private Course buildCourse(Long id, RunnectUser owner, boolean isPrivate) {
        Course course = Course.builder()
            .runnectUser(owner)
            .title("코스 제목")
            .departureRegion("경기")
            .departureCity("시흥시")
            .departureTown("정왕동")
            .departureDetail("정왕본동")
            .departureName("정왕역")
            .distance(5.2f)
            .image("https://image.example/course.png")
            .path(validLineString())
            .build();
        ReflectionTestUtils.setField(course, "id", id);
        if (!isPrivate) {
            ReflectionTestUtils.setField(course, "isPrivate", false);
        }
        return course;
    }

    @Nested
    @DisplayName("createCourse")
    class CreateCourse {

        @Test
        @DisplayName("정상 요청이면 코스를 저장하고 유저 코스 카운트/스탬프를 갱신한다")
        void 정상_생성() {
            RunnectUser user = buildUser(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            Course savedCourse = buildCourse(100L, user, true);
            ReflectionTestUtils.setField(savedCourse, "createdAt", LocalDateTime.of(2026, 1, 1, 0, 0));
            when(courseRepository.save(any(Course.class))).thenReturn(savedCourse);

            CourseCreateRequestDto requestDto = createRequestDto(validPath(), "경기 시흥시 정왕동");

            CourseCreateResponseDto response = courseService.createCourse(1L, requestDto, "image-key.png");

            assertThat(response.getId()).isEqualTo(100L);
            assertThat(response.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 0, 0));
            verify(userStampService).recordActivityAndAwardStamp(1L, StampType.c);

            ArgumentCaptor<Course> captor = ArgumentCaptor.forClass(Course.class);
            verify(courseRepository).save(captor.capture());
            Course passed = captor.getValue();
            assertThat(passed.getRunnectUser()).isEqualTo(user);
            assertThat(passed.getTitle()).isEqualTo("정왕역 코스");
            assertThat(passed.getDepartureRegion()).isEqualTo("경기");
            assertThat(passed.getDepartureCity()).isEqualTo("시흥시");
            assertThat(passed.getDepartureTown()).isEqualTo("정왕동");
            assertThat(passed.getImage()).isEqualTo("image-key.png");
            assertThat(passed.getPath()).isNotNull();
        }

        @Test
        @DisplayName("존재하지 않는 유저면 NotFoundUserException")
        void 존재하지_않는_유저() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> courseService.createCourse(1L,
                createRequestDto(validPath(), "경기 시흥시 정왕동"), "img"))
                .isInstanceOf(NotFoundUserException.class);

            verify(courseRepository, never()).save(any());
        }

        @Test
        @DisplayName("좌표가 1개 뿐이면 LineString을 만들 수 없어 BadRequestException")
        void 좌표가_부족하면_경로_생성_실패() {
            RunnectUser user = buildUser(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            List<CoordinateDto> onePoint = Collections.singletonList(new CoordinateDto(37.5665, 126.9780));

            assertThatThrownBy(() -> courseService.createCourse(1L,
                createRequestDto(onePoint, "경기 시흥시 정왕동"), "img"))
                .isInstanceOf(BadRequestException.class);

            verify(courseRepository, never()).save(any());
        }

        @Test
        @DisplayName("출발지 주소가 3토큰 미만이면 BadRequestException")
        void 출발지_주소가_불완전하면_BadRequestException() {
            RunnectUser user = buildUser(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> courseService.createCourse(1L,
                createRequestDto(validPath(), "경기 시흥시"), "img"))
                .isInstanceOf(BadRequestException.class);

            verify(courseRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getCourseByUser")
    class GetCourseByUser {

        @Test
        @DisplayName("유저의 코스 목록을 CourseResponse로 매핑해 반환한다")
        void 정상_조회() {
            RunnectUser user = buildUser(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            Course course1 = buildCourse(10L, user, true);
            Course course2 = buildCourse(11L, user, true);
            when(courseRepository.findCourseByUserId(1L)).thenReturn(Arrays.asList(course1, course2));

            CourseGetByUserResponseDto response = courseService.getCourseByUser(1L);

            assertThat(response.getUser().getId()).isEqualTo(1L);
            assertThat(response.getCourses()).hasSize(2)
                .extracting("id")
                .containsExactly(10L, 11L);
            assertThat(response.getCourses().get(0).getDeparture().getRegion()).isEqualTo("경기");
            assertThat(response.getCourses().get(0).getDeparture().getCity()).isEqualTo("시흥시");
        }

        @Test
        @DisplayName("코스가 없으면 빈 목록을 반환한다")
        void 코스가_없으면_빈_목록() {
            RunnectUser user = buildUser(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(courseRepository.findCourseByUserId(1L)).thenReturn(Collections.emptyList());

            CourseGetByUserResponseDto response = courseService.getCourseByUser(1L);

            assertThat(response.getCourses()).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 유저면 NotFoundUserException")
        void 존재하지_않는_유저() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> courseService.getCourseByUser(1L))
                .isInstanceOf(NotFoundUserException.class);
        }
    }

    @Nested
    @DisplayName("getPrivateCourseByUser")
    class GetPrivateCourseByUser {

        @Test
        @DisplayName("비공개 코스 전용 조회 메서드를 호출한다")
        void 정상_조회() {
            RunnectUser user = buildUser(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            Course course = buildCourse(10L, user, true);
            when(courseRepository.findCourseByUserIdOnlyPrivate(1L)).thenReturn(
                Collections.singletonList(course));

            CourseGetByUserResponseDto response = courseService.getPrivateCourseByUser(1L);

            assertThat(response.getCourses()).hasSize(1);
            verify(courseRepository).findCourseByUserIdOnlyPrivate(1L);
            verify(courseRepository, never()).findCourseByUserId(any());
        }

        @Test
        @DisplayName("존재하지 않는 유저면 NotFoundUserException")
        void 존재하지_않는_유저() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> courseService.getPrivateCourseByUser(1L))
                .isInstanceOf(NotFoundUserException.class);
        }
    }

    @Nested
    @DisplayName("getCourseDetail")
    class GetCourseDetail {

        @Test
        @DisplayName("본인이 업로드한 코스면 isNowUser가 true다")
        void 본인_코스() {
            RunnectUser user = buildUser(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            Course course = buildCourse(10L, user, true);
            when(courseRepository.findById(10L)).thenReturn(Optional.of(course));

            GetCourseDetailResponseDto response = courseService.getCourseDetail(10L, 1L);

            assertThat(response.getCourse().getIsNowUser()).isTrue();
            assertThat(response.getUser().getUserId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("다른 사람이 업로드한 코스면 isNowUser가 false다")
        void 타인_코스() {
            RunnectUser requester = buildUser(1L);
            RunnectUser uploader = buildUser(2L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
            Course course = buildCourse(10L, uploader, true);
            when(courseRepository.findById(10L)).thenReturn(Optional.of(course));

            GetCourseDetailResponseDto response = courseService.getCourseDetail(10L, 1L);

            assertThat(response.getCourse().getIsNowUser()).isFalse();
        }

        @Test
        @DisplayName("업로더와 요청자의 id가 같으면 객체 인스턴스가 달라도 같은 사람으로 판정된다")
        void 같은_id면_인스턴스가_달라도_같은_사람으로_판정된다() {
            RunnectUser requester = buildUser(1L);
            RunnectUser uploaderWithSameIdButDifferentInstance = buildUser(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
            Course course = buildCourse(10L, uploaderWithSameIdButDifferentInstance, true);
            when(courseRepository.findById(10L)).thenReturn(Optional.of(course));

            GetCourseDetailResponseDto response = courseService.getCourseDetail(10L, 1L);

            assertThat(response.getCourse().getIsNowUser()).isTrue();
        }

        @Test
        @DisplayName("업로더 정보가 없는 코스는 isNowUser가 false이고 userId는 -1이다")
        void 업로더가_없는_코스() {
            RunnectUser requester = buildUser(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
            Course course = buildCourse(10L, null, true);
            when(courseRepository.findById(10L)).thenReturn(Optional.of(course));

            GetCourseDetailResponseDto response = courseService.getCourseDetail(10L, 1L);

            assertThat(response.getCourse().getIsNowUser()).isFalse();
            assertThat(response.getUser().getUserId()).isEqualTo(-1L);
        }

        @Test
        @DisplayName("존재하지 않는 유저면 NotFoundUserException")
        void 존재하지_않는_유저() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> courseService.getCourseDetail(10L, 1L))
                .isInstanceOf(NotFoundUserException.class);
        }

        @Test
        @DisplayName("존재하지 않는 코스면 NotFoundException")
        void 존재하지_않는_코스() {
            RunnectUser user = buildUser(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(courseRepository.findById(10L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> courseService.getCourseDetail(10L, 1L))
                .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateCourse")
    class UpdateCourse {

        @Test
        @DisplayName("코스 제목을 수정한다")
        void 정상_수정() {
            RunnectUser user = buildUser(1L);
            Course course = buildCourse(10L, user, true);
            when(courseRepository.findByCourseIdAndUserId(10L, 1L)).thenReturn(Optional.of(course));

            UpdateCourseResponseDto response = courseService.updateCourse(1L, 10L, "새 제목");

            assertThat(course.getTitle()).isEqualTo("새 제목");
            assertThat(response.getCourse().getTitle()).isEqualTo("새 제목");
            assertThat(response.getCourse().getId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("존재하지 않는 코스면 NotFoundException")
        void 존재하지_않는_코스() {
            when(courseRepository.findByCourseIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> courseService.updateCourse(1L, 10L, "새 제목"))
                .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("코스 소유자가 아니면 NotFoundException (IDOR 방지)")
        void 소유자가_아니면_수정_불가() {
            Long 다른유저Id = 999L;
            when(courseRepository.findByCourseIdAndUserId(10L, 다른유저Id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> courseService.updateCourse(다른유저Id, 10L, "남의 코스 제목 변경"))
                .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteCourses")
    class DeleteCourses {

        @Test
        @DisplayName("비공개 코스는 soft delete만 수행한다")
        void 비공개_코스_삭제() {
            RunnectUser user = buildUser(1L);
            Course course = buildCourse(10L, user, true);
            when(courseRepository.findByCourseIdAndUserId(10L, 1L)).thenReturn(Optional.of(course));

            DeleteCoursesResponseDto response = courseService.deleteCourses(
                Collections.singletonList(10L), 1L);

            assertThat(response.getDeletedCourseCount()).isEqualTo(1);
            assertThat(course.getDeletedAt()).isNotNull();
            verify(publicCourseRepository, never()).delete(any());
        }

        @Test
        @DisplayName("공개된 코스는 공개 코스를 함께 삭제하고 비공개로 되돌린 뒤 soft delete한다")
        void 공개_코스_삭제() {
            RunnectUser user = buildUser(1L);
            Course course = buildCourse(10L, user, false);
            PublicCourse publicCourse = mock(PublicCourse.class);
            ReflectionTestUtils.setField(course, "publicCourse", publicCourse);
            when(courseRepository.findByCourseIdAndUserId(10L, 1L)).thenReturn(Optional.of(course));

            courseService.deleteCourses(Collections.singletonList(10L), 1L);

            verify(publicCourseRepository).delete(publicCourse);
            assertThat(course.getIsPrivate()).isTrue();
            assertThat(course.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("여러 개를 한 번에 삭제하면 개수가 그대로 반환된다")
        void 여러개_삭제() {
            RunnectUser user = buildUser(1L);
            Course course1 = buildCourse(10L, user, true);
            Course course2 = buildCourse(11L, user, true);
            when(courseRepository.findByCourseIdAndUserId(10L, 1L)).thenReturn(Optional.of(course1));
            when(courseRepository.findByCourseIdAndUserId(11L, 1L)).thenReturn(Optional.of(course2));

            DeleteCoursesResponseDto response = courseService.deleteCourses(Arrays.asList(10L, 11L), 1L);

            assertThat(response.getDeletedCourseCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("본인 소유가 아니거나 존재하지 않는 코스면 NotFoundException")
        void 존재하지_않거나_소유자가_아닌_코스() {
            when(courseRepository.findByCourseIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> courseService.deleteCourses(Collections.singletonList(10L), 1L))
                .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("빈 목록을 요청하면 아무 것도 삭제하지 않고 0을 반환한다")
        void 빈_목록() {
            DeleteCoursesResponseDto response = courseService.deleteCourses(Collections.emptyList(), 1L);

            assertThat(response.getDeletedCourseCount()).isEqualTo(0);
            verify(courseRepository, never()).findByCourseIdAndUserId(any(), any());
        }

        @Test
        @DisplayName("목록 중간에 실패하면 그 이후 항목은 조회조차 되지 않고 예외가 전파된다")
        void 중간에_실패하면_이후_항목은_처리되지_않는다() {
            RunnectUser user = buildUser(1L);
            Course course1 = buildCourse(10L, user, true);
            when(courseRepository.findByCourseIdAndUserId(10L, 1L)).thenReturn(Optional.of(course1));
            when(courseRepository.findByCourseIdAndUserId(11L, 1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> courseService.deleteCourses(Arrays.asList(10L, 11L, 12L), 1L))
                .isInstanceOf(NotFoundException.class);

            assertThat(course1.getDeletedAt()).isNotNull();
            verify(courseRepository, never()).findByCourseIdAndUserId(12L, 1L);
        }
    }
}
