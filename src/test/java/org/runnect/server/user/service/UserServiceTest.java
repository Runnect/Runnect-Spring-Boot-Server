package org.runnect.server.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.LineString;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.runnect.server.auth.service.AppleSignInService;
import org.runnect.server.common.exception.UnauthorizedException;
import org.runnect.server.common.module.convert.CoordinateDto;
import org.runnect.server.common.module.convert.CoordinatePathConverter;
import org.runnect.server.course.entity.Course;
import org.runnect.server.course.repository.CourseRepository;
import org.runnect.server.publicCourse.entity.PublicCourse;
import org.runnect.server.scrap.repository.ScrapRepository;
import org.runnect.server.user.dto.request.UpdateUserNicknameRequestDto;
import org.runnect.server.user.dto.response.DeleteUserResponseDto;
import org.runnect.server.user.dto.response.MyPageResponseDto;
import org.runnect.server.user.dto.response.UpdateUserNicknameResponseDto;
import org.runnect.server.user.dto.response.UserProfileResponseDto;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.entity.SocialType;
import org.runnect.server.user.entity.UserStamp;
import org.runnect.server.user.exception.userException.DuplicateNicknameException;
import org.runnect.server.user.exception.userException.NotFoundUserException;
import org.runnect.server.user.repository.UserRepository;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ScrapRepository scrapRepository;
    @Mock
    private AppleSignInService appleSignInService;
    @Mock
    private CourseRepository courseRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, scrapRepository, appleSignInService, courseRepository);
    }

    private RunnectUser buildUser(Long id, String nickname, SocialType provider) {
        RunnectUser user = RunnectUser.builder()
            .nickname(nickname)
            .socialId("social-" + id)
            .email("user" + id + "@runnect.io")
            .provider(provider)
            .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private RunnectUser buildUser(Long id) {
        return buildUser(id, "러너" + id, SocialType.KAKAO);
    }

    private void setStampCount(RunnectUser user, int count) {
        List<UserStamp> stamps = Stream.generate(() -> mock(UserStamp.class))
            .limit(count)
            .collect(Collectors.toList());
        ReflectionTestUtils.setField(user, "userStamps", stamps);
    }

    private LineString validLineString() {
        return CoordinatePathConverter.coorConvertPath(java.util.Arrays.asList(
            new CoordinateDto(37.5665, 126.9780),
            new CoordinateDto(37.5651, 126.9895)
        ));
    }

    private Course buildCourse(Long id, RunnectUser owner) {
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
        ReflectionTestUtils.setField(course, "isPrivate", false);
        return course;
    }

    private PublicCourse buildPublicCourse(Long id, Course course) {
        PublicCourse publicCourse = PublicCourse.builder()
            .course(course)
            .title("공개 코스")
            .description("설명")
            .build();
        ReflectionTestUtils.setField(publicCourse, "id", id);
        ReflectionTestUtils.setField(course, "publicCourse", publicCourse);
        return publicCourse;
    }

    @Nested
    @DisplayName("getMyPage")
    class GetMyPage {

        @Test
        @DisplayName("정상 조회 시 유저 정보와 레벨 퍼센트를 반환한다")
        void 정상_조회() {
            RunnectUser user = buildUser(1L);
            setStampCount(user, 5);
            when(userRepository.findUserByIdWithUserStamps(1L)).thenReturn(Optional.of(user));

            MyPageResponseDto response = userService.getMyPage(1L);

            assertThat(response.getUser().getId()).isEqualTo(1L);
            assertThat(response.getUser().getLevelPercent()).isEqualTo(25);
        }

        @Test
        @DisplayName("스탬프가 0개면 레벨 퍼센트는 0이다")
        void 스탬프_없음() {
            RunnectUser user = buildUser(1L);
            setStampCount(user, 0);
            when(userRepository.findUserByIdWithUserStamps(1L)).thenReturn(Optional.of(user));

            MyPageResponseDto response = userService.getMyPage(1L);

            assertThat(response.getUser().getLevelPercent()).isEqualTo(0);
        }

        @Test
        @DisplayName("스탬프가 4개면 한 바퀴 돌아서 레벨 퍼센트는 다시 0이다")
        void 스탬프_4개면_한바퀴() {
            RunnectUser user = buildUser(1L);
            setStampCount(user, 4);
            when(userRepository.findUserByIdWithUserStamps(1L)).thenReturn(Optional.of(user));

            MyPageResponseDto response = userService.getMyPage(1L);

            assertThat(response.getUser().getLevelPercent()).isEqualTo(0);
        }

        @Test
        @DisplayName("존재하지 않는 유저면 NotFoundUserException")
        void 존재하지_않는_유저() {
            when(userRepository.findUserByIdWithUserStamps(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getMyPage(1L))
                .isInstanceOf(NotFoundUserException.class);
        }
    }

    @Nested
    @DisplayName("updateUserNickname")
    class UpdateUserNickname {

        @Test
        @DisplayName("중복되지 않는 새 닉네임이면 정상 변경된다")
        void 정상_변경() {
            RunnectUser user = buildUser(1L, "기존닉네임", SocialType.KAKAO);
            setStampCount(user, 0);
            when(userRepository.findUserByIdWithUserStamps(1L)).thenReturn(Optional.of(user));
            when(userRepository.existsByNickname("새닉네임")).thenReturn(false);

            UpdateUserNicknameResponseDto response = userService.updateUserNickname(1L,
                new UpdateUserNicknameRequestDto("새닉네임"));

            assertThat(user.getNickname()).isEqualTo("새닉네임");
            assertThat(response.getUser().getNickname()).isEqualTo("새닉네임");
        }

        @Test
        @DisplayName("이미 존재하는 닉네임이면 DuplicateNicknameException")
        void 중복_닉네임() {
            RunnectUser user = buildUser(1L, "기존닉네임", SocialType.KAKAO);
            when(userRepository.findUserByIdWithUserStamps(1L)).thenReturn(Optional.of(user));
            when(userRepository.existsByNickname("중복닉네임")).thenReturn(true);

            assertThatThrownBy(() -> userService.updateUserNickname(1L,
                new UpdateUserNicknameRequestDto("중복닉네임")))
                .isInstanceOf(DuplicateNicknameException.class);

            assertThat(user.getNickname()).isEqualTo("기존닉네임");
        }

        @Test
        @DisplayName("본인의 현재 닉네임으로 다시 저장해도 중복 처리하지 않는다")
        void 본인_현재_닉네임으로_재저장() {
            RunnectUser user = buildUser(1L, "내닉네임", SocialType.KAKAO);
            setStampCount(user, 0);
            when(userRepository.findUserByIdWithUserStamps(1L)).thenReturn(Optional.of(user));

            UpdateUserNicknameResponseDto response = userService.updateUserNickname(1L,
                new UpdateUserNicknameRequestDto("내닉네임"));

            assertThat(response.getUser().getNickname()).isEqualTo("내닉네임");
            verify(userRepository, never()).existsByNickname(org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("존재하지 않는 유저면 NotFoundUserException")
        void 존재하지_않는_유저() {
            when(userRepository.findUserByIdWithUserStamps(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUserNickname(1L,
                new UpdateUserNicknameRequestDto("새닉네임")))
                .isInstanceOf(NotFoundUserException.class);
        }
    }

    @Nested
    @DisplayName("getUserProfile")
    class GetUserProfile {

        @Test
        @DisplayName("프로필 유저가 공개한 코스 목록을 스크랩 여부와 함께 반환한다")
        void 정상_조회() {
            RunnectUser profileUser = buildUser(1L);
            setStampCount(profileUser, 0);
            RunnectUser requestUser = buildUser(2L);
            Course course = buildCourse(10L, profileUser);
            PublicCourse publicCourse = buildPublicCourse(100L, course);

            when(userRepository.findUserByIdWithUserStamps(1L)).thenReturn(Optional.of(profileUser));
            when(userRepository.findById(2L)).thenReturn(Optional.of(requestUser));
            when(scrapRepository.getScrappedTruePublicCourseIds(requestUser)).thenReturn(
                Collections.singletonList(100L));
            when(courseRepository.findCoursesForUserProfile(profileUser)).thenReturn(
                Collections.singletonList(course));

            UserProfileResponseDto response = userService.getUserProfile(1L, 2L);

            assertThat(response.getUser().getUserId()).isEqualTo(1L);
            assertThat(response.getCourses()).hasSize(1);
            assertThat(response.getCourses().get(0).getScrapTF()).isTrue();
        }

        @Test
        @DisplayName("스크랩하지 않은 코스는 scrapTF가 false다")
        void 스크랩_안함() {
            RunnectUser profileUser = buildUser(1L);
            setStampCount(profileUser, 0);
            RunnectUser requestUser = buildUser(2L);
            Course course = buildCourse(10L, profileUser);
            buildPublicCourse(100L, course);

            when(userRepository.findUserByIdWithUserStamps(1L)).thenReturn(Optional.of(profileUser));
            when(userRepository.findById(2L)).thenReturn(Optional.of(requestUser));
            when(scrapRepository.getScrappedTruePublicCourseIds(requestUser)).thenReturn(
                Collections.emptyList());
            when(courseRepository.findCoursesForUserProfile(profileUser)).thenReturn(
                Collections.singletonList(course));

            UserProfileResponseDto response = userService.getUserProfile(1L, 2L);

            assertThat(response.getCourses().get(0).getScrapTF()).isFalse();
        }

        @Test
        @DisplayName("공개한 코스가 없으면 빈 목록을 반환한다")
        void 공개한_코스_없음() {
            RunnectUser profileUser = buildUser(1L);
            setStampCount(profileUser, 0);
            RunnectUser requestUser = buildUser(2L);

            when(userRepository.findUserByIdWithUserStamps(1L)).thenReturn(Optional.of(profileUser));
            when(userRepository.findById(2L)).thenReturn(Optional.of(requestUser));
            when(scrapRepository.getScrappedTruePublicCourseIds(requestUser)).thenReturn(
                Collections.emptyList());
            when(courseRepository.findCoursesForUserProfile(profileUser)).thenReturn(Collections.emptyList());

            UserProfileResponseDto response = userService.getUserProfile(1L, 2L);

            assertThat(response.getCourses()).isEmpty();
        }

        @Test
        @DisplayName("프로필 대상 유저가 없으면 NotFoundUserException")
        void 프로필_유저_없음() {
            when(userRepository.findUserByIdWithUserStamps(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserProfile(1L, 2L))
                .isInstanceOf(NotFoundUserException.class);
        }

        @Test
        @DisplayName("요청 유저가 없으면 NotFoundUserException")
        void 요청_유저_없음() {
            RunnectUser profileUser = buildUser(1L);
            when(userRepository.findUserByIdWithUserStamps(1L)).thenReturn(Optional.of(profileUser));
            when(userRepository.findById(2L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserProfile(1L, 2L))
                .isInstanceOf(NotFoundUserException.class);
        }
    }

    @Nested
    @DisplayName("deleteUser")
    class DeleteUser {

        @Test
        @DisplayName("일반 소셜 유저는 애플 관련 처리 없이 바로 삭제된다")
        void 일반_유저_삭제() {
            RunnectUser user = buildUser(1L, "러너", SocialType.KAKAO);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            DeleteUserResponseDto response = userService.deleteUser(1L, null);

            assertThat(response.getDeletedUserId()).isEqualTo(1L);
            verify(appleSignInService, never()).reportWithdrawalToApple(org.mockito.ArgumentMatchers.any());
            verify(userRepository).delete(user);
        }

        @Test
        @DisplayName("애플 유저는 accessToken이 있으면 애플에 탈퇴를 알리고 삭제된다")
        void 애플_유저_토큰_있음() {
            RunnectUser user = buildUser(1L, "러너", SocialType.APPLE);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            userService.deleteUser(1L, "apple-token");

            verify(appleSignInService).reportWithdrawalToApple("apple-token");
            verify(userRepository).delete(user);
        }

        @Test
        @DisplayName("애플 유저인데 accessToken이 없으면 UnauthorizedException")
        void 애플_유저_토큰_없음() {
            RunnectUser user = buildUser(1L, "러너", SocialType.APPLE);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> userService.deleteUser(1L, null))
                .isInstanceOf(UnauthorizedException.class);

            verify(userRepository, never()).delete(org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("존재하지 않는 유저면 NotFoundUserException")
        void 존재하지_않는_유저() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.deleteUser(1L, null))
                .isInstanceOf(NotFoundUserException.class);
        }
    }
}
