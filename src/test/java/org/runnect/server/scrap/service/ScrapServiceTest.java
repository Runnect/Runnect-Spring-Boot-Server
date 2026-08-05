package org.runnect.server.scrap.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.LineString;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.runnect.server.common.exception.NotFoundException;
import org.runnect.server.common.module.convert.CoordinateDto;
import org.runnect.server.common.module.convert.CoordinatePathConverter;
import org.runnect.server.course.entity.Course;
import org.runnect.server.publicCourse.entity.PublicCourse;
import org.runnect.server.publicCourse.repository.PublicCourseRepository;
import org.runnect.server.scrap.dto.request.CreateAndDeleteScrapRequestDto;
import org.runnect.server.scrap.dto.response.CreateAndDeleteScrapResponseDto;
import org.runnect.server.scrap.dto.response.GetScrapCourseResponseDto;
import org.runnect.server.scrap.entity.Scrap;
import org.runnect.server.scrap.repository.ScrapRepository;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.entity.SocialType;
import org.runnect.server.user.entity.StampType;
import org.runnect.server.user.exception.userException.NotFoundUserException;
import org.runnect.server.user.repository.UserRepository;
import org.runnect.server.user.service.UserStampService;
import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ScrapServiceTest {

    @Mock
    private ScrapRepository scrapRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PublicCourseRepository publicCourseRepository;
    @Mock
    private UserStampService userStampService;

    private ScrapService scrapService;

    @BeforeEach
    void setUp() {
        scrapService = new ScrapService(scrapRepository, userRepository, publicCourseRepository,
            userStampService);
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

    private LineString validLineString() {
        return CoordinatePathConverter.coorConvertPath(Arrays.asList(
            new CoordinateDto(37.5665, 126.9780),
            new CoordinateDto(37.5651, 126.9895)
        ));
    }

    private PublicCourse buildPublicCourse(Long id, RunnectUser owner) {
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
        ReflectionTestUtils.setField(course, "id", 10L);
        ReflectionTestUtils.setField(course, "isPrivate", false);

        PublicCourse publicCourse = PublicCourse.builder()
            .course(course)
            .title("공개 코스")
            .description("설명")
            .build();
        ReflectionTestUtils.setField(publicCourse, "id", id);
        return publicCourse;
    }

    private Scrap buildScrap(Long id, RunnectUser user, PublicCourse publicCourse, boolean scrapTF) {
        Scrap scrap = Scrap.builder().runnectUser(user).publicCourse(publicCourse).scrapTF(scrapTF).build();
        ReflectionTestUtils.setField(scrap, "id", id);
        return scrap;
    }

    private CreateAndDeleteScrapRequestDto requestDto(Long publicCourseId, boolean scrapTF) {
        CreateAndDeleteScrapRequestDto dto = BeanUtils.instantiateClass(CreateAndDeleteScrapRequestDto.class);
        ReflectionTestUtils.setField(dto, "publicCourseId", publicCourseId);
        ReflectionTestUtils.setField(dto, "scrapTF", scrapTF);
        return dto;
    }

    @Nested
    @DisplayName("createAndDeleteScrap")
    class CreateAndDeleteScrap {

        @Test
        @DisplayName("스크랩한 적 없는 코스를 새로 스크랩하면 신규 생성되고 유저 정보가 갱신된다")
        void 신규_스크랩() {
            RunnectUser user = buildUser(1L);
            PublicCourse publicCourse = buildPublicCourse(100L, user);
            when(scrapRepository.findByUserIdAndPublicCourseId(1L, 100L)).thenReturn(Optional.empty());
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(publicCourseRepository.findById(100L)).thenReturn(Optional.of(publicCourse));
            when(scrapRepository.countByPublicCourseAndScrapTFIsTrue(publicCourse)).thenReturn(1L);

            CreateAndDeleteScrapResponseDto response = scrapService.createAndDeleteScrap(1L,
                requestDto(100L, true));

            assertThat(response.getScrapCount()).isEqualTo(1L);
            assertThat(response.getScrapTF()).isTrue();
            assertThat(user.getCreatedScrap()).isEqualTo(1L);
            verify(userStampService).createStampByUser(user, StampType.s);
            verify(scrapRepository).save(any(Scrap.class));
        }

        @Test
        @DisplayName("이미 취소된 스크랩을 다시 스크랩하면 기존 행을 재활성화하고 새로 생성하지 않는다")
        void 기존_스크랩_재활성화() {
            RunnectUser user = buildUser(1L);
            PublicCourse publicCourse = buildPublicCourse(100L, user);
            Scrap existingScrap = buildScrap(50L, user, publicCourse, false);
            when(scrapRepository.findByUserIdAndPublicCourseId(1L, 100L)).thenReturn(Optional.of(existingScrap));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(publicCourseRepository.findById(100L)).thenReturn(Optional.of(publicCourse));
            when(scrapRepository.countByPublicCourseAndScrapTFIsTrue(publicCourse)).thenReturn(1L);

            scrapService.createAndDeleteScrap(1L, requestDto(100L, true));

            assertThat(existingScrap.getScrapTF()).isTrue();
            assertThat(user.getCreatedScrap()).isEqualTo(0L);
            verify(scrapRepository, never()).save(any());
            verify(userStampService, never()).createStampByUser(any(), any());
        }

        @Test
        @DisplayName("스크랩한 코스를 취소하면 scrapTF가 false가 된다")
        void 스크랩_취소() {
            RunnectUser user = buildUser(1L);
            PublicCourse publicCourse = buildPublicCourse(100L, user);
            Scrap existingScrap = buildScrap(50L, user, publicCourse, true);
            when(scrapRepository.findByUserIdAndPublicCourseId(1L, 100L)).thenReturn(Optional.of(existingScrap));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(publicCourseRepository.findById(100L)).thenReturn(Optional.of(publicCourse));
            when(scrapRepository.countByPublicCourseAndScrapTFIsTrue(publicCourse)).thenReturn(0L);

            scrapService.createAndDeleteScrap(1L, requestDto(100L, false));

            assertThat(existingScrap.getScrapTF()).isFalse();
        }

        @Test
        @DisplayName("스크랩한 적 없는 코스를 취소 요청해도 예외 없이 처리된다")
        void 스크랩한_적_없는_코스_취소_요청은_무시된다() {
            RunnectUser user = buildUser(1L);
            PublicCourse publicCourse = buildPublicCourse(100L, user);
            when(scrapRepository.findByUserIdAndPublicCourseId(1L, 100L)).thenReturn(Optional.empty());
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(publicCourseRepository.findById(100L)).thenReturn(Optional.of(publicCourse));
            when(scrapRepository.countByPublicCourseAndScrapTFIsTrue(publicCourse)).thenReturn(0L);

            assertThatCode(() -> scrapService.createAndDeleteScrap(1L, requestDto(100L, false)))
                .doesNotThrowAnyException();

            verify(scrapRepository, never()).save(any());
        }

        @Test
        @DisplayName("존재하지 않는 유저면 NotFoundUserException")
        void 존재하지_않는_유저() {
            RunnectUser dummyOwner = buildUser(2L);
            PublicCourse publicCourse = buildPublicCourse(100L, dummyOwner);
            when(scrapRepository.findByUserIdAndPublicCourseId(1L, 100L)).thenReturn(Optional.empty());
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> scrapService.createAndDeleteScrap(1L, requestDto(100L, true)))
                .isInstanceOf(NotFoundUserException.class);
        }

        @Test
        @DisplayName("존재하지 않는 공개 코스면 NotFoundException")
        void 존재하지_않는_공개코스() {
            RunnectUser user = buildUser(1L);
            when(scrapRepository.findByUserIdAndPublicCourseId(1L, 100L)).thenReturn(Optional.empty());
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(publicCourseRepository.findById(100L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> scrapService.createAndDeleteScrap(1L, requestDto(100L, true)))
                .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getScrapCourseByUser")
    class GetScrapCourseByUser {

        @Test
        @DisplayName("스크랩한 코스 목록을 매핑해서 반환한다")
        void 정상_조회() {
            RunnectUser user = buildUser(1L);
            PublicCourse publicCourse = buildPublicCourse(100L, user);
            Scrap scrap = buildScrap(50L, user, publicCourse, true);
            when(scrapRepository.findAllByUserIdAndScrapTF(1L)).thenReturn(
                Optional.of(Collections.singletonList(scrap)));

            GetScrapCourseResponseDto response = scrapService.getScrapCourseByUser(1L);

            assertThat(response.getUser().getUserId()).isEqualTo(1L);
            assertThat(response.getScraps()).hasSize(1);
            assertThat(response.getScraps().get(0).getPublicCourseId()).isEqualTo(100L);
            assertThat(response.getScraps().get(0).getDeparture().getRegion()).isEqualTo("경기");
        }

        @Test
        @DisplayName("스크랩한 코스가 없으면 빈 목록을 반환한다")
        void 스크랩_없음() {
            when(scrapRepository.findAllByUserIdAndScrapTF(1L)).thenReturn(Optional.of(Collections.emptyList()));

            GetScrapCourseResponseDto response = scrapService.getScrapCourseByUser(1L);

            assertThat(response.getScraps()).isEmpty();
        }

        @Test
        @DisplayName("레포지토리가 빈 Optional을 반환하면 NotFoundException")
        void 조회_결과가_없으면_예외() {
            when(scrapRepository.findAllByUserIdAndScrapTF(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> scrapService.getScrapCourseByUser(1L))
                .isInstanceOf(NotFoundException.class);
        }
    }
}
