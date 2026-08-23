package org.runnect.server.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.runnect.server.user.dto.response.GetUserStampsResponseDto;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.entity.SocialType;
import org.runnect.server.user.entity.StampType;
import org.runnect.server.user.entity.UserStamp;
import org.runnect.server.user.exception.userException.NotFoundUserException;
import org.runnect.server.user.repository.UserRepository;
import org.runnect.server.user.repository.UserStampRepository;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserStampServiceTest {

    @Mock
    private UserStampRepository userStampRepository;
    @Mock
    private UserRepository userRepository;

    private UserStampService userStampService;

    @BeforeEach
    void setUp() {
        userStampService = new UserStampService(userStampRepository, userRepository);
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

    @Nested
    @DisplayName("createStampByUser")
    class CreateStampByUser {

        @Test
        @DisplayName("활동 카운트가 0이면 아무 것도 하지 않는다")
        void 활동카운트_0() {
            RunnectUser user = buildUser(1L);

            userStampService.createStampByUser(user, StampType.c);

            verify(userStampRepository, never()).save(any());
        }

        @Test
        @DisplayName("코스 1개째면 c1 스탬프가 생성된다")
        void 코스_1개_c1() {
            RunnectUser user = buildUser(1L);
            user.updateCreatedCourse();

            userStampService.createStampByUser(user, StampType.c);

            org.mockito.ArgumentCaptor<UserStamp> captor = org.mockito.ArgumentCaptor.forClass(UserStamp.class);
            verify(userStampRepository).save(captor.capture());
            assertThat(captor.getValue().getStampId()).isEqualTo(StampType.c1);
        }

        @Test
        @DisplayName("코스 10개째면 c2 스탬프가 생성된다")
        void 코스_10개_c2() {
            RunnectUser user = buildUser(1L);
            for (int i = 0; i < 10; i++) {
                user.updateCreatedCourse();
            }

            userStampService.createStampByUser(user, StampType.c);

            org.mockito.ArgumentCaptor<UserStamp> captor = org.mockito.ArgumentCaptor.forClass(UserStamp.class);
            verify(userStampRepository).save(captor.capture());
            assertThat(captor.getValue().getStampId()).isEqualTo(StampType.c2);
        }

        @Test
        @DisplayName("코스 30개째면 c3 스탬프가 생성된다")
        void 코스_30개_c3() {
            RunnectUser user = buildUser(1L);
            for (int i = 0; i < 30; i++) {
                user.updateCreatedCourse();
            }

            userStampService.createStampByUser(user, StampType.c);

            org.mockito.ArgumentCaptor<UserStamp> captor = org.mockito.ArgumentCaptor.forClass(UserStamp.class);
            verify(userStampRepository).save(captor.capture());
            assertThat(captor.getValue().getStampId()).isEqualTo(StampType.c3);
        }

        @Test
        @DisplayName("기준 개수(1/10/30)가 아니면 스탬프를 생성하지 않는다")
        void 기준개수가_아니면_생성안함() {
            RunnectUser user = buildUser(1L);
            for (int i = 0; i < 5; i++) {
                user.updateCreatedCourse();
            }

            userStampService.createStampByUser(user, StampType.c);

            verify(userStampRepository, never()).save(any());
        }

        @Test
        @DisplayName("스크랩 타입(s)은 createdScrap 기준으로 판단한다")
        void 스크랩_타입() {
            RunnectUser user = buildUser(1L);
            user.updateCreatedScrap();

            userStampService.createStampByUser(user, StampType.s);

            org.mockito.ArgumentCaptor<UserStamp> captor = org.mockito.ArgumentCaptor.forClass(UserStamp.class);
            verify(userStampRepository).save(captor.capture());
            assertThat(captor.getValue().getStampId()).isEqualTo(StampType.s1);
        }

        @Test
        @DisplayName("기록 타입(r)은 createdRecord 기준으로 판단한다")
        void 기록_타입() {
            RunnectUser user = buildUser(1L);
            user.updateCreatedRecord();

            userStampService.createStampByUser(user, StampType.r);

            org.mockito.ArgumentCaptor<UserStamp> captor = org.mockito.ArgumentCaptor.forClass(UserStamp.class);
            verify(userStampRepository).save(captor.capture());
            assertThat(captor.getValue().getStampId()).isEqualTo(StampType.r1);
        }

        @Test
        @DisplayName("업로드 타입(u)은 createdPublicCourse 기준으로 판단한다")
        void 업로드_타입() {
            RunnectUser user = buildUser(1L);
            user.updateCreatedPublicCourse();

            userStampService.createStampByUser(user, StampType.u);

            org.mockito.ArgumentCaptor<UserStamp> captor = org.mockito.ArgumentCaptor.forClass(UserStamp.class);
            verify(userStampRepository).save(captor.capture());
            assertThat(captor.getValue().getStampId()).isEqualTo(StampType.u1);
        }

        @Test
        @DisplayName("[설계상 주의] 이미 레벨이 붙은 타입(c1 등)으로 호출하면 조용히 무시된다")
        void 레벨_붙은_타입으로_호출하면_무시됨() {
            RunnectUser user = buildUser(1L);
            user.updateCreatedCourse();

            userStampService.createStampByUser(user, StampType.c1);

            verify(userStampRepository, never()).save(any());
        }

        @Test
        @DisplayName("누적 스탬프 개수가 4의 배수(12 이하)가 되면 레벨업한다")
        void 스탬프_4개면_레벨업() {
            RunnectUser user = buildUser(1L);
            user.updateCreatedCourse();
            when(userStampRepository.countByRunnectUser(user)).thenReturn(4L);

            userStampService.createStampByUser(user, StampType.c);

            assertThat(user.getLevel()).isEqualTo(2);
        }

        @Test
        @DisplayName("누적 스탬프 개수가 8이면 레벨 3이 된다")
        void 스탬프_8개면_레벨3() {
            RunnectUser user = buildUser(1L);
            user.updateCreatedCourse();
            when(userStampRepository.countByRunnectUser(user)).thenReturn(8L);

            userStampService.createStampByUser(user, StampType.c);

            assertThat(user.getLevel()).isEqualTo(3);
        }

        @Test
        @DisplayName("누적 스탬프 개수가 12를 넘으면 더 이상 레벨업하지 않는다")
        void 스탬프_12_초과면_레벨업_안함() {
            RunnectUser user = buildUser(1L);
            user.updateCreatedCourse();
            int levelBefore = user.getLevel();
            when(userStampRepository.countByRunnectUser(user)).thenReturn(16L);

            userStampService.createStampByUser(user, StampType.c);

            assertThat(user.getLevel()).isEqualTo(levelBefore);
        }

        @Test
        @DisplayName("스탬프 개수가 4의 배수가 아니면 레벨업하지 않는다")
        void 배수가_아니면_레벨업_안함() {
            RunnectUser user = buildUser(1L);
            user.updateCreatedCourse();
            int levelBefore = user.getLevel();
            when(userStampRepository.countByRunnectUser(user)).thenReturn(5L);

            userStampService.createStampByUser(user, StampType.c);

            assertThat(user.getLevel()).isEqualTo(levelBefore);
        }
    }

    @Nested
    @DisplayName("recordActivityAndAwardStamp")
    class RecordActivityAndAwardStamp {

        @Test
        @DisplayName("userId로 유저를 다시 조회해서 카운터를 증가시키고 saveAndFlush로 저장한다")
        void 정상_호출() {
            RunnectUser user = buildUser(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            userStampService.recordActivityAndAwardStamp(1L, StampType.c);

            assertThat(user.getCreatedCourse()).isEqualTo(1L);
            verify(userRepository).saveAndFlush(user);
        }

        @Test
        @DisplayName("존재하지 않는 유저면 NotFoundUserException")
        void 존재하지_않는_유저() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userStampService.recordActivityAndAwardStamp(1L, StampType.c))
                .isInstanceOf(NotFoundUserException.class);
        }
    }

    @Nested
    @DisplayName("findUserStamps")
    class FindUserStamps {

        @Test
        @DisplayName("정상 조회 시 유저의 스탬프 목록을 반환한다")
        void 정상_조회() {
            RunnectUser user = buildUser(1L);
            when(userRepository.findUserByIdWithUserStamps(1L)).thenReturn(Optional.of(user));

            GetUserStampsResponseDto response = userStampService.findUserStamps(1L);

            // GetUserStampsResponseDto.UserInfo/StampInfo는 private static class라
            // 외부(테스트)에서 타입을 직접 다룰 수 없어 리플렉션으로 필드를 확인한다.
            Object userInfo = ReflectionTestUtils.invokeMethod(response, "getUser");
            assertThat(ReflectionTestUtils.getField(userInfo, "id")).isEqualTo(1L);
            assertThat(response.getStamps()).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 유저면 NotFoundUserException")
        void 존재하지_않는_유저() {
            when(userRepository.findUserByIdWithUserStamps(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userStampService.findUserStamps(1L))
                .isInstanceOf(NotFoundUserException.class);
        }
    }
}
