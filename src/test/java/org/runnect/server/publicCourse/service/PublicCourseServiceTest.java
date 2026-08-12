package org.runnect.server.publicCourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.runnect.server.common.exception.BadRequestException;
import org.runnect.server.common.exception.ConflictException;
import org.runnect.server.common.exception.NotFoundException;
import org.runnect.server.common.exception.PermissionDeniedException;
import org.runnect.server.common.module.convert.CoordinateDto;
import org.runnect.server.common.module.convert.CoordinatePathConverter;
import org.runnect.server.course.entity.Course;
import org.runnect.server.course.repository.CourseRepository;
import org.runnect.server.publicCourse.dto.request.CreatePublicCourseRequestDto;
import org.runnect.server.publicCourse.dto.request.DeletePublicCoursesRequestDto;
import org.runnect.server.publicCourse.dto.response.CreatePublicCourseResponseDto;
import org.runnect.server.publicCourse.dto.response.DeletePublicCoursesResponseDto;
import org.runnect.server.publicCourse.dto.response.GetPublicCourseDetailResponseDto;
import org.runnect.server.publicCourse.dto.response.GetPublicCourseTotalPageCountResponseDto;
import org.runnect.server.publicCourse.dto.response.UpdatePublicCourseResponseDto;
import org.runnect.server.publicCourse.dto.response.getMarathonPublicCourse.GetMarathonPublicCourseResponseDto;
import org.runnect.server.publicCourse.dto.response.getPublicCourseByUser.GetPublicCourseByUserResponseDto;
import org.runnect.server.publicCourse.dto.response.recommendPublicCourse.RecommendPublicCourseResponseDto;
import org.runnect.server.publicCourse.dto.response.searchPublicCourse.SearchPublicCourseResponseDto;
import org.runnect.server.publicCourse.entity.PublicCourse;
import org.runnect.server.publicCourse.repository.PublicCourseRepository;
import org.runnect.server.scrap.entity.Scrap;
import org.runnect.server.scrap.repository.ScrapRepository;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.entity.SocialType;
import org.runnect.server.user.exception.userException.NotFoundUserException;
import org.runnect.server.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PublicCourseServiceTest {

    @Mock
    private PublicCourseRepository publicCourseRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ScrapRepository scrapRepository;
    @Mock
    private CourseRepository courseRepository;

    private PublicCourseService publicCourseService;

    @BeforeEach
    void setUp() {
        publicCourseService = new PublicCourseService(publicCourseRepository, userRepository, scrapRepository,
            courseRepository);
        ReflectionTestUtils.invokeMethod(publicCourseService, "setMARATHON_PUBLIC_COURSE_IDS", "100,200");
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

    private Course buildCourse(Long id, RunnectUser owner, boolean isPrivate, String departureName) {
        Course course = Course.builder()
            .runnectUser(owner)
            .title("코스 제목")
            .departureRegion("경기")
            .departureCity("시흥시")
            .departureTown("정왕동")
            .departureDetail("정왕본동")
            .departureName(departureName)
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

    private Course buildCourse(Long id, RunnectUser owner, boolean isPrivate) {
        return buildCourse(id, owner, isPrivate, "정왕역");
    }

    private PublicCourse buildPublicCourse(Long id, Course course) {
        PublicCourse publicCourse = PublicCourse.builder()
            .course(course)
            .title("공개 코스 제목")
            .description("설명")
            .build();
        ReflectionTestUtils.setField(publicCourse, "id", id);
        return publicCourse;
    }

    private Scrap buildScrap(RunnectUser user, PublicCourse publicCourse) {
        return Scrap.builder().runnectUser(user).publicCourse(publicCourse).scrapTF(true).build();
    }

    @Nested
    @DisplayName("getPublicCourseTotalPageCount")
    class GetPublicCourseTotalPageCount {

        @Test
        @DisplayName("정확히 나누어 떨어지면 그대로 페이지 수가 된다")
        void 나누어_떨어짐() {
            when(publicCourseRepository.countBy()).thenReturn(20L);

            GetPublicCourseTotalPageCountResponseDto response = publicCourseService.getPublicCourseTotalPageCount();

            assertThat(response.getTotalPageCount()).isEqualTo(2L);
        }

        @Test
        @DisplayName("나누어 떨어지지 않으면 올림해서 한 페이지를 더한다")
        void 나누어_안_떨어짐() {
            when(publicCourseRepository.countBy()).thenReturn(21L);

            GetPublicCourseTotalPageCountResponseDto response = publicCourseService.getPublicCourseTotalPageCount();

            assertThat(response.getTotalPageCount()).isEqualTo(3L);
        }

        @Test
        @DisplayName("코스가 없으면 0페이지다")
        void 코스가_없음() {
            when(publicCourseRepository.countBy()).thenReturn(0L);

            GetPublicCourseTotalPageCountResponseDto response = publicCourseService.getPublicCourseTotalPageCount();

            assertThat(response.getTotalPageCount()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("getMarathonPublicCourse")
    class GetMarathonPublicCourse {

        @Test
        @DisplayName("마라톤 코스 목록을 스크랩 여부와 함께 반환한다")
        void 정상_조회() {
            RunnectUser user = buildUser(1L);
            Course course1 = buildCourse(10L, user, false);
            Course course2 = buildCourse(11L, user, false);
            PublicCourse pc1 = buildPublicCourse(100L, course1);
            PublicCourse pc2 = buildPublicCourse(200L, course2);

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(scrapRepository.findAllByUserIdAndScrapTF(1L)).thenReturn(
                Optional.of(Collections.singletonList(buildScrap(user, buildPublicCourse(100L, course1)))));
            when(publicCourseRepository.findByIdIn(Arrays.asList(100L, 200L))).thenReturn(Arrays.asList(pc1, pc2));

            GetMarathonPublicCourseResponseDto response = publicCourseService.getMarathonPublicCourse(1L);

            assertThat(response.getMarathonPublicCourses()).hasSize(2);
            assertThat(response.getMarathonPublicCourses().get(0).getScrap()).isTrue();
            assertThat(response.getMarathonPublicCourses().get(1).getScrap()).isFalse();
        }

        @Test
        @DisplayName("설정된 마라톤 코스 중 일부가 존재하지 않으면 NotFoundException")
        void 마라톤_코스_일부_없음() {
            RunnectUser user = buildUser(1L);
            Course course1 = buildCourse(10L, user, false);
            PublicCourse pc1 = buildPublicCourse(100L, course1);

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(scrapRepository.findAllByUserIdAndScrapTF(1L)).thenReturn(Optional.of(Collections.emptyList()));
            when(publicCourseRepository.findByIdIn(Arrays.asList(100L, 200L))).thenReturn(
                Collections.singletonList(pc1));

            assertThatThrownBy(() -> publicCourseService.getMarathonPublicCourse(1L))
                .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("존재하지 않는 유저면 NotFoundUserException")
        void 존재하지_않는_유저() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> publicCourseService.getMarathonPublicCourse(1L))
                .isInstanceOf(NotFoundUserException.class);
        }
    }

    @Nested
    @DisplayName("searchPublicCourse")
    class SearchPublicCourse {

        @Test
        @DisplayName("키워드로 검색된 코스를 스크랩 여부와 함께 반환한다")
        void 정상_검색() {
            RunnectUser user = buildUser(1L);
            Course course = buildCourse(10L, user, false);
            PublicCourse publicCourse = buildPublicCourse(100L, course);

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(scrapRepository.findAllByUserIdAndScrapTF(1L)).thenReturn(
                Optional.of(Collections.singletonList(buildScrap(user, buildPublicCourse(100L, course)))));
            when(publicCourseRepository.searchPublicCourseByKeyword("정왕")).thenReturn(
                Collections.singletonList(publicCourse));

            SearchPublicCourseResponseDto response = publicCourseService.searchPublicCourse(1L, "정왕");

            assertThat(response.getPublicCourses()).hasSize(1);
            assertThat(response.getPublicCourses().get(0).getScrap()).isTrue();
        }

        @Test
        @DisplayName("검색 결과가 없으면 빈 목록을 반환한다")
        void 검색_결과_없음() {
            RunnectUser user = buildUser(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(scrapRepository.findAllByUserIdAndScrapTF(1L)).thenReturn(Optional.of(Collections.emptyList()));
            when(publicCourseRepository.searchPublicCourseByKeyword("없는키워드")).thenReturn(Collections.emptyList());

            SearchPublicCourseResponseDto response = publicCourseService.searchPublicCourse(1L, "없는키워드");

            assertThat(response.getPublicCourses()).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 유저면 NotFoundUserException")
        void 존재하지_않는_유저() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> publicCourseService.searchPublicCourse(1L, "정왕"))
                .isInstanceOf(NotFoundUserException.class);
        }
    }

    @Nested
    @DisplayName("recommendPublicCourse")
    class RecommendPublicCourse {

        @Test
        @DisplayName("scrap 정렬로 조회한다")
        void 스크랩순_정렬() {
            RunnectUser user = buildUser(1L);
            Course course = buildCourse(10L, user, false);
            PublicCourse publicCourse = buildPublicCourse(100L, course);
            Page<PublicCourse> page = new PageImpl<>(Collections.singletonList(publicCourse),
                PageRequest.of(0, 10), 1);

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(scrapRepository.findAllByUserIdAndScrapTF(1L)).thenReturn(Optional.of(Collections.emptyList()));
            when(publicCourseRepository.findAll(any(Pageable.class))).thenReturn(page);

            RecommendPublicCourseResponseDto response = publicCourseService.recommendPublicCourse(1L, 1, "scrap");

            assertThat(response.getPublicCourses()).hasSize(1);
            assertThat(response.getOrdering()).isEqualTo("scrap");
            assertThat(response.getIsEnd()).isTrue();
        }

        @Test
        @DisplayName("date 정렬로 조회한다")
        void 최신순_정렬() {
            RunnectUser user = buildUser(1L);
            Page<PublicCourse> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(scrapRepository.findAllByUserIdAndScrapTF(1L)).thenReturn(Optional.of(Collections.emptyList()));
            when(publicCourseRepository.findAll(any(Pageable.class))).thenReturn(page);

            RecommendPublicCourseResponseDto response = publicCourseService.recommendPublicCourse(1L, 1, "date");

            assertThat(response.getOrdering()).isEqualTo("date");
        }

        @Test
        @DisplayName("정렬 값이 scrap/date 둘 다 아니면 BadRequestException")
        void 잘못된_정렬값() {
            RunnectUser user = buildUser(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(scrapRepository.findAllByUserIdAndScrapTF(1L)).thenReturn(Optional.of(Collections.emptyList()));

            assertThatThrownBy(() -> publicCourseService.recommendPublicCourse(1L, 1, "인기순"))
                .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("존재하지 않는 유저면 NotFoundUserException")
        void 존재하지_않는_유저() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> publicCourseService.recommendPublicCourse(1L, 1, "scrap"))
                .isInstanceOf(NotFoundUserException.class);
        }
    }

    @Nested
    @DisplayName("getPublicCourseByUser")
    class GetPublicCourseByUser {

        @Test
        @DisplayName("유저가 공개한 코스 목록을 반환한다")
        void 정상_조회() {
            RunnectUser user = buildUser(1L);
            Course course = buildCourse(10L, user, false);
            PublicCourse publicCourse = buildPublicCourse(100L, course);
            ReflectionTestUtils.setField(course, "publicCourse", publicCourse);

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(courseRepository.findCoursesByRunnectUserAndIsPrivateIsFalseAndDeletedAtIsNull(user))
                .thenReturn(Collections.singletonList(course));
            when(scrapRepository.findAllByUserIdAndScrapTF(1L)).thenReturn(Optional.of(Collections.emptyList()));

            GetPublicCourseByUserResponseDto response = publicCourseService.getPublicCourseByUser(1L);

            assertThat(response.getUser().getId()).isEqualTo(1L);
            assertThat(response.getPublicCourses()).hasSize(1);
            assertThat(response.getPublicCourses().get(0).getId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("공개한 코스가 없으면 빈 목록을 반환한다")
        void 공개한_코스_없음() {
            RunnectUser user = buildUser(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(courseRepository.findCoursesByRunnectUserAndIsPrivateIsFalseAndDeletedAtIsNull(user))
                .thenReturn(Collections.emptyList());
            when(scrapRepository.findAllByUserIdAndScrapTF(1L)).thenReturn(Optional.of(Collections.emptyList()));

            GetPublicCourseByUserResponseDto response = publicCourseService.getPublicCourseByUser(1L);

            assertThat(response.getPublicCourses()).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 유저면 NotFoundUserException")
        void 존재하지_않는_유저() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> publicCourseService.getPublicCourseByUser(1L))
                .isInstanceOf(NotFoundUserException.class);
        }
    }

    @Nested
    @DisplayName("getPublicCourseDetail")
    class GetPublicCourseDetail {

        @Test
        @DisplayName("정상 조회 시 출발지 건물명이 있으면 포함해서 반환한다")
        void 정상_조회_건물명_있음() {
            RunnectUser user = buildUser(1L);
            Course course = buildCourse(10L, user, false, "정왕역");
            PublicCourse publicCourse = buildPublicCourse(100L, course);

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(publicCourseRepository.findById(100L)).thenReturn(Optional.of(publicCourse));
            when(scrapRepository.findAllByUserIdAndScrapTF(1L)).thenReturn(Optional.of(Collections.emptyList()));

            GetPublicCourseDetailResponseDto response = publicCourseService.getPublicCourseDetail(1L, 100L);

            assertThat(response.getUser().getId()).isEqualTo(1L);
            assertThat(response.getUser().getIsNowUser()).isTrue();
            assertThat(response.getPublicCourse().getId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("출발지 건물명이 없어도 정상 조회된다")
        void 정상_조회_건물명_없음() {
            RunnectUser user = buildUser(1L);
            Course course = buildCourse(10L, user, false, null);
            PublicCourse publicCourse = buildPublicCourse(100L, course);

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(publicCourseRepository.findById(100L)).thenReturn(Optional.of(publicCourse));
            when(scrapRepository.findAllByUserIdAndScrapTF(1L)).thenReturn(Optional.of(Collections.emptyList()));

            GetPublicCourseDetailResponseDto response = publicCourseService.getPublicCourseDetail(1L, 100L);

            assertThat(response.getPublicCourse().getId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("삭제된 코스면 NotFoundException")
        void 삭제된_코스() {
            RunnectUser user = buildUser(1L);
            Course course = buildCourse(10L, user, false);
            ReflectionTestUtils.setField(course, "deletedAt", LocalDateTime.of(2026, 1, 1, 0, 0));
            PublicCourse publicCourse = buildPublicCourse(100L, course);

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(publicCourseRepository.findById(100L)).thenReturn(Optional.of(publicCourse));

            assertThatThrownBy(() -> publicCourseService.getPublicCourseDetail(1L, 100L))
                .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("다른 사람이 올린 코스면 isNowUser가 false다")
        void 타인_코스() {
            RunnectUser uploader = buildUser(1L);
            RunnectUser requester = buildUser(2L);
            Course course = buildCourse(10L, uploader, false);
            PublicCourse publicCourse = buildPublicCourse(100L, course);

            when(userRepository.findById(2L)).thenReturn(Optional.of(requester));
            when(publicCourseRepository.findById(100L)).thenReturn(Optional.of(publicCourse));
            when(scrapRepository.findAllByUserIdAndScrapTF(2L)).thenReturn(Optional.of(Collections.emptyList()));

            GetPublicCourseDetailResponseDto response = publicCourseService.getPublicCourseDetail(2L, 100L);

            assertThat(response.getUser().getIsNowUser()).isFalse();
        }

        @Test
        @DisplayName("업로더가 탈퇴한 코스면 '알 수 없음' 유저로 대체된다")
        void 업로더가_없는_코스() {
            RunnectUser requester = buildUser(1L);
            Course course = buildCourse(10L, null, false);
            PublicCourse publicCourse = buildPublicCourse(100L, course);

            when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
            when(publicCourseRepository.findById(100L)).thenReturn(Optional.of(publicCourse));
            when(scrapRepository.findAllByUserIdAndScrapTF(1L)).thenReturn(Optional.of(Collections.emptyList()));

            GetPublicCourseDetailResponseDto response = publicCourseService.getPublicCourseDetail(1L, 100L);

            assertThat(response.getUser().getNickname()).isEqualTo("알 수 없음");
            assertThat(response.getUser().getIsNowUser()).isFalse();
        }

        @Test
        @DisplayName("본인이 스크랩한 코스면 scrap이 true다")
        void 스크랩_매칭() {
            RunnectUser user = buildUser(1L);
            Course course = buildCourse(10L, user, false);
            PublicCourse publicCourse = buildPublicCourse(100L, course);

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(publicCourseRepository.findById(100L)).thenReturn(Optional.of(publicCourse));
            when(scrapRepository.findAllByUserIdAndScrapTF(1L)).thenReturn(
                Optional.of(Collections.singletonList(buildScrap(user, buildPublicCourse(100L, course)))));

            GetPublicCourseDetailResponseDto response = publicCourseService.getPublicCourseDetail(1L, 100L);

            assertThat(response.getPublicCourse().getScrap()).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 유저면 NotFoundUserException")
        void 존재하지_않는_유저() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> publicCourseService.getPublicCourseDetail(1L, 100L))
                .isInstanceOf(NotFoundUserException.class);
        }

        @Test
        @DisplayName("존재하지 않는 공개 코스면 NotFoundException")
        void 존재하지_않는_공개코스() {
            RunnectUser user = buildUser(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(publicCourseRepository.findById(100L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> publicCourseService.getPublicCourseDetail(1L, 100L))
                .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("createPublicCourse")
    class CreatePublicCourse {

        @Test
        @DisplayName("본인 소유의 비공개 코스를 정상적으로 공개한다")
        void 정상_생성() {
            RunnectUser user = buildUser(1L);
            Course course = buildCourse(10L, user, true);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
            when(publicCourseRepository.save(any(PublicCourse.class))).thenAnswer(invocation -> {
                PublicCourse saved = invocation.getArgument(0);
                ReflectionTestUtils.setField(saved, "id", 100L);
                ReflectionTestUtils.setField(saved, "createdAt", LocalDateTime.of(2026, 1, 1, 0, 0));
                return saved;
            });

            CreatePublicCourseRequestDto request = new CreatePublicCourseRequestDto(10L, "제목", "설명");

            CreatePublicCourseResponseDto response = publicCourseService.createPublicCourse(1L, request);

            assertThat(response.getPublicCourse()).isNotNull();
            assertThat(course.getIsPrivate()).isFalse();
        }

        @Test
        @DisplayName("존재하지 않는 유저면 NotFoundUserException")
        void 존재하지_않는_유저() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> publicCourseService.createPublicCourse(1L,
                new CreatePublicCourseRequestDto(10L, "제목", "설명")))
                .isInstanceOf(NotFoundUserException.class);
        }

        @Test
        @DisplayName("존재하지 않는 코스면 NotFoundException")
        void 존재하지_않는_코스() {
            RunnectUser user = buildUser(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(courseRepository.findById(10L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> publicCourseService.createPublicCourse(1L,
                new CreatePublicCourseRequestDto(10L, "제목", "설명")))
                .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("본인이 그린 코스가 아니면 PermissionDeniedException")
        void 소유자가_아님() {
            RunnectUser user = buildUser(1L);
            Course course = buildCourse(10L, buildUser(2L), true);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(courseRepository.findById(10L)).thenReturn(Optional.of(course));

            assertThatThrownBy(() -> publicCourseService.createPublicCourse(1L,
                new CreatePublicCourseRequestDto(10L, "제목", "설명")))
                .isInstanceOf(PermissionDeniedException.class);
        }

        @Test
        @DisplayName("이미 공개된 코스면 ConflictException")
        void 이미_공개된_코스() {
            RunnectUser user = buildUser(1L);
            Course course = buildCourse(10L, user, false);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(courseRepository.findById(10L)).thenReturn(Optional.of(course));

            assertThatThrownBy(() -> publicCourseService.createPublicCourse(1L,
                new CreatePublicCourseRequestDto(10L, "제목", "설명")))
                .isInstanceOf(ConflictException.class);
        }
    }

    @Nested
    @DisplayName("deletePublicCourses")
    class DeletePublicCourses {

        @Test
        @DisplayName("본인 소유 공개 코스를 정상 삭제한다")
        void 정상_삭제() {
            RunnectUser user = buildUser(1L);
            Course course = buildCourse(10L, user, false);
            PublicCourse publicCourse = buildPublicCourse(100L, course);

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(publicCourseRepository.findByIdInWithCourseAndRecords(Collections.singletonList(100L))).thenReturn(
                Collections.singletonList(publicCourse));

            DeletePublicCoursesResponseDto response = publicCourseService.deletePublicCourses(1L,
                new DeletePublicCoursesRequestDto(Collections.singletonList(100L)));

            assertThat(response.getDeletedPublicCourseCount()).isEqualTo(1);
            assertThat(course.getIsPrivate()).isTrue();
            verify(scrapRepository).deleteByPublicCourseIn(Collections.singletonList(publicCourse));
            verify(publicCourseRepository).deleteAll(Collections.singletonList(publicCourse));
        }

        @Test
        @DisplayName("존재하지 않는 id가 포함되면 NotFoundException")
        void 존재하지_않는_공개코스_포함() {
            RunnectUser user = buildUser(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(publicCourseRepository.findByIdInWithCourseAndRecords(Arrays.asList(100L, 999L))).thenReturn(Collections.emptyList());

            assertThatThrownBy(() -> publicCourseService.deletePublicCourses(1L,
                new DeletePublicCoursesRequestDto(Arrays.asList(100L, 999L))))
                .isInstanceOf(NotFoundException.class);

            verify(publicCourseRepository, never()).deleteAll(any());
        }

        @Test
        @DisplayName("본인 소유가 아닌 코스가 섞여 있으면 PermissionDeniedException")
        void 소유자가_아닌_코스_포함() {
            RunnectUser user = buildUser(1L);
            RunnectUser otherUser = buildUser(2L);
            PublicCourse ownPublicCourse = buildPublicCourse(100L, buildCourse(10L, user, false));
            PublicCourse othersPublicCourse = buildPublicCourse(101L, buildCourse(11L, otherUser, false));

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(publicCourseRepository.findByIdInWithCourseAndRecords(Arrays.asList(100L, 101L))).thenReturn(
                Arrays.asList(ownPublicCourse, othersPublicCourse));

            assertThatThrownBy(() -> publicCourseService.deletePublicCourses(1L,
                new DeletePublicCoursesRequestDto(Arrays.asList(100L, 101L))))
                .isInstanceOf(PermissionDeniedException.class);

            verify(publicCourseRepository, never()).deleteAll(any());
        }

        @Test
        @DisplayName("관리자는 본인 소유가 아니어도 삭제할 수 있다")
        void 관리자는_소유자가_아니어도_삭제_가능() {
            Long adminId = 280L;
            RunnectUser admin = buildUser(adminId);
            RunnectUser otherUser = buildUser(2L);
            PublicCourse othersPublicCourse = buildPublicCourse(101L, buildCourse(11L, otherUser, false));

            when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
            when(publicCourseRepository.findByIdInWithCourseAndRecords(Collections.singletonList(101L))).thenReturn(
                Collections.singletonList(othersPublicCourse));

            DeletePublicCoursesResponseDto response = publicCourseService.deletePublicCourses(adminId,
                new DeletePublicCoursesRequestDto(Collections.singletonList(101L)));

            assertThat(response.getDeletedPublicCourseCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("존재하지 않는 유저면 NotFoundUserException")
        void 존재하지_않는_유저() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> publicCourseService.deletePublicCourses(1L,
                new DeletePublicCoursesRequestDto(Collections.singletonList(100L))))
                .isInstanceOf(NotFoundUserException.class);
        }
    }

    @Nested
    @DisplayName("updatePublicCourse")
    class UpdatePublicCourse {

        @Test
        @DisplayName("본인 소유 공개 코스면 제목/설명을 수정한다")
        void 정상_수정() {
            RunnectUser user = buildUser(1L);
            PublicCourse publicCourse = buildPublicCourse(100L, buildCourse(10L, user, false));
            when(publicCourseRepository.findById(100L)).thenReturn(Optional.of(publicCourse));

            UpdatePublicCourseResponseDto response = publicCourseService.updatePublicCourse(1L, 100L, "새 제목",
                "새 설명");

            assertThat(publicCourse.getTitle()).isEqualTo("새 제목");
            assertThat(publicCourse.getDescription()).isEqualTo("새 설명");
            assertThat(response.getPublicCourse().getTitle()).isEqualTo("새 제목");
        }

        @Test
        @DisplayName("존재하지 않는 공개 코스면 NotFoundException")
        void 존재하지_않는_공개코스() {
            when(publicCourseRepository.findById(100L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> publicCourseService.updatePublicCourse(1L, 100L, "새 제목", "새 설명"))
                .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("본인 소유가 아니면 PermissionDeniedException (IDOR 방지)")
        void 소유자가_아니면_수정_불가() {
            RunnectUser owner = buildUser(1L);
            PublicCourse publicCourse = buildPublicCourse(100L, buildCourse(10L, owner, false));
            when(publicCourseRepository.findById(100L)).thenReturn(Optional.of(publicCourse));

            Long 다른유저Id = 999L;

            assertThatThrownBy(() -> publicCourseService.updatePublicCourse(다른유저Id, 100L, "남의 코스", "수정 시도"))
                .isInstanceOf(PermissionDeniedException.class);

            assertThat(publicCourse.getTitle()).isEqualTo("공개 코스 제목");
        }

        @Test
        @DisplayName("관리자는 본인 소유가 아니어도 수정할 수 있다")
        void 관리자는_소유자가_아니어도_수정_가능() {
            Long adminId = 280L;
            RunnectUser owner = buildUser(1L);
            PublicCourse publicCourse = buildPublicCourse(100L, buildCourse(10L, owner, false));
            when(publicCourseRepository.findById(100L)).thenReturn(Optional.of(publicCourse));

            UpdatePublicCourseResponseDto response = publicCourseService.updatePublicCourse(adminId, 100L,
                "관리자 수정", "관리자 설명");

            assertThat(response.getPublicCourse().getTitle()).isEqualTo("관리자 수정");
        }
    }
}
