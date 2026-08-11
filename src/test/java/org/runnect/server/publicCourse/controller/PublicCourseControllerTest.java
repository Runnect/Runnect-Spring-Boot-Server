package org.runnect.server.publicCourse.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.runnect.server.common.constant.ErrorStatus;
import org.runnect.server.common.constant.TokenStatus;
import org.runnect.server.common.exception.BadRequestException;
import org.runnect.server.common.exception.NotFoundException;
import org.runnect.server.common.exception.PermissionDeniedException;
import org.runnect.server.config.jwt.JwtService;
import org.runnect.server.config.slack.SlackApi;
import org.runnect.server.publicCourse.dto.response.CreatePublicCourseResponseDto;
import org.runnect.server.publicCourse.dto.response.DeletePublicCoursesResponseDto;
import org.runnect.server.publicCourse.dto.response.GetPublicCourseTotalPageCountResponseDto;
import org.runnect.server.publicCourse.dto.response.UpdatePublicCourseResponseDto;
import org.runnect.server.publicCourse.dto.response.getMarathonPublicCourse.GetMarathonPublicCourseResponseDto;
import org.runnect.server.publicCourse.dto.response.getPublicCourseByUser.GetPublicCourseByUserResponseDto;
import org.runnect.server.publicCourse.dto.response.recommendPublicCourse.RecommendPublicCourseResponseDto;
import org.runnect.server.publicCourse.dto.response.searchPublicCourse.SearchPublicCourseResponseDto;
import org.runnect.server.publicCourse.entity.PublicCourse;
import org.runnect.server.publicCourse.service.PublicCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(PublicCourseController.class)
class PublicCourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PublicCourseService publicCourseService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private SlackApi slackApi;

    @BeforeEach
    void setUpAuth() {
        BDDMockito.lenient().when(jwtService.verifyToken("valid")).thenReturn(TokenStatus.TOKEN_VALID);
        BDDMockito.lenient().when(jwtService.getJwtContents("valid")).thenReturn("1");
    }

    private <B extends MockHttpServletRequestBuilder> B withAuth(B builder) {
        builder.header("accessToken", "valid").header("refreshToken", "valid");
        return builder;
    }

    @Nested
    @DisplayName("GET /api/public-course")
    class RecommendPublicCourse {

        @Test
        @DisplayName("정상 요청이면 200과 추천 코스 목록을 반환한다")
        void 정상_조회() throws Exception {
            when(publicCourseService.recommendPublicCourse(eq(1L), eq(1), eq("date")))
                .thenReturn(RecommendPublicCourseResponseDto.of("date", 3, false, Collections.emptyList()));

            mockMvc.perform(withAuth(get("/api/public-course")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ordering").value("date"));
        }

        @Test
        @DisplayName("sort 파라미터가 유효하지 않으면 400을 반환한다")
        void 정렬값_유효하지_않음() throws Exception {
            mockMvc.perform(withAuth(get("/api/public-course?sort=invalid")))
                .andExpect(status().isBadRequest());

            BDDMockito.verifyNoInteractions(publicCourseService);
        }

        @Test
        @DisplayName("[설계상 주의] pageNo에 @Positive가 있어도 클래스에 @Validated가 없어 실제로는 검증되지 않는다")
        void 페이지번호_0이어도_검증되지_않음() throws Exception {
            when(publicCourseService.recommendPublicCourse(eq(1L), eq(0), eq("date")))
                .thenReturn(RecommendPublicCourseResponseDto.of("date", 3, false, Collections.emptyList()));

            mockMvc.perform(withAuth(get("/api/public-course").param("pageNo", "0")))
                .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/public-course/search")
    class SearchPublicCourse {

        @Test
        @DisplayName("정상 요청이면 200과 검색 결과를 반환한다")
        void 정상_검색() throws Exception {
            when(publicCourseService.searchPublicCourse(eq(1L), eq("정왕역")))
                .thenReturn(SearchPublicCourseResponseDto.of(Collections.emptyList()));

            mockMvc.perform(withAuth(get("/api/public-course/search").param("keyword", "정왕역")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.publicCourses").isEmpty());
        }

        @Test
        @DisplayName("keyword가 없으면 400을 반환한다")
        void 검색어_없음() throws Exception {
            mockMvc.perform(withAuth(get("/api/public-course/search")))
                .andExpect(status().isBadRequest());

            BDDMockito.verifyNoInteractions(publicCourseService);
        }
    }

    @Nested
    @DisplayName("GET /api/public-course/marathon")
    class GetMarathonPublicCourse {

        @Test
        @DisplayName("정상 요청이면 200과 마라톤 코스 목록을 반환한다")
        void 정상_조회() throws Exception {
            when(publicCourseService.getMarathonPublicCourse(1L))
                .thenReturn(GetMarathonPublicCourseResponseDto.of(Collections.emptyList()));

            mockMvc.perform(withAuth(get("/api/public-course/marathon")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.marathonPublicCourses").isEmpty());
        }
    }

    @Nested
    @DisplayName("POST /api/public-course")
    class CreatePublicCourse {

        @Test
        @DisplayName("정상 요청이면 201과 생성된 퍼블릭 코스 정보를 반환한다")
        void 정상_생성() throws Exception {
            when(publicCourseService.createPublicCourse(eq(1L), any()))
                .thenReturn(CreatePublicCourseResponseDto.of(100L, "2026-01-01T00:00:00"));

            mockMvc.perform(withAuth(post("/api/public-course"))
                    .contentType("application/json")
                    .content("{\"courseId\":10,\"title\":\"정왕역 코스\",\"description\":\"설명\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.publicCourse.id").value(100));
        }

        @Test
        @DisplayName("title이 없으면 400을 반환한다")
        void 제목_없음() throws Exception {
            mockMvc.perform(withAuth(post("/api/public-course"))
                    .contentType("application/json")
                    .content("{\"courseId\":10,\"description\":\"설명\"}"))
                .andExpect(status().isBadRequest());

            BDDMockito.verifyNoInteractions(publicCourseService);
        }
    }

    @Nested
    @DisplayName("GET /api/public-course/detail/{publicCourseId}")
    class GetPublicCourseDetail {

        @Test
        @DisplayName("존재하지 않는 퍼블릭 코스면 400을 반환한다")
        void 존재하지_않음() throws Exception {
            when(publicCourseService.getPublicCourseDetail(anyLong(), eq(999L)))
                .thenThrow(new NotFoundException(
                    ErrorStatus.NOT_FOUND_PUBLIC_COURSE_EXCEPTION,
                    ErrorStatus.NOT_FOUND_PUBLIC_COURSE_EXCEPTION.getMessage()));

            mockMvc.perform(withAuth(get("/api/public-course/detail/999")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("GET /api/public-course/user")
    class GetPublicCourseByUser {

        @Test
        @DisplayName("정상 요청이면 200과 유저의 퍼블릭 코스 목록을 반환한다")
        void 정상_조회() throws Exception {
            when(publicCourseService.getPublicCourseByUser(1L))
                .thenReturn(GetPublicCourseByUserResponseDto.of(1L, Collections.emptyList()));

            mockMvc.perform(withAuth(get("/api/public-course/user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.id").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/public-course/total-page-count")
    class GetPublicCourseTotalPageCount {

        @Test
        @DisplayName("정상 요청이면 200과 전체 페이지 수를 반환한다")
        void 정상_조회() throws Exception {
            when(publicCourseService.getPublicCourseTotalPageCount())
                .thenReturn(GetPublicCourseTotalPageCountResponseDto.of(5L));

            mockMvc.perform(get("/api/public-course/total-page-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalPageCount").value(5));
        }
    }

    @Nested
    @DisplayName("PUT /api/public-course")
    class DeletePublicCourses {

        @Test
        @DisplayName("정상 요청이면 200과 삭제 개수를 반환한다")
        void 정상_삭제() throws Exception {
            when(publicCourseService.deletePublicCourses(eq(1L), any()))
                .thenReturn(DeletePublicCoursesResponseDto.from(1));

            mockMvc.perform(withAuth(put("/api/public-course"))
                    .contentType("application/json")
                    .content("{\"publicCourseIdList\":[10]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deletedPublicCourseCount").value(1));
        }

        @Test
        @DisplayName("publicCourseIdList가 비어있으면 400을 반환한다")
        void 목록_비어있음() throws Exception {
            mockMvc.perform(withAuth(put("/api/public-course"))
                    .contentType("application/json")
                    .content("{\"publicCourseIdList\":[]}"))
                .andExpect(status().isBadRequest());

            BDDMockito.verifyNoInteractions(publicCourseService);
        }
    }

    @Nested
    @DisplayName("PATCH /api/public-course/{publicCourseId}")
    class UpdatePublicCourse {

        @Test
        @DisplayName("정상 요청이면 200과 수정된 정보를 반환한다")
        void 정상_수정() throws Exception {
            PublicCourse publicCourse = PublicCourse.builder().title("새 제목").description("새 설명").build();
            ReflectionTestUtils.setField(publicCourse, "id", 10L);
            when(publicCourseService.updatePublicCourse(eq(1L), eq(10L), eq("새 제목"), eq("새 설명")))
                .thenReturn(UpdatePublicCourseResponseDto.of(publicCourse));

            mockMvc.perform(withAuth(patch("/api/public-course/10"))
                    .contentType("application/json")
                    .content("{\"title\":\"새 제목\",\"description\":\"새 설명\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.publicCourse.title").value("새 제목"));
        }

        @Test
        @DisplayName("title이 없으면 400을 반환한다")
        void 제목_없음() throws Exception {
            mockMvc.perform(withAuth(patch("/api/public-course/10"))
                    .contentType("application/json")
                    .content("{\"title\":\"\",\"description\":\"설명\"}"))
                .andExpect(status().isBadRequest());

            BDDMockito.verifyNoInteractions(publicCourseService);
        }

        @Test
        @DisplayName("소유권이 없으면 403을 반환한다")
        void 소유권_없음() throws Exception {
            when(publicCourseService.updatePublicCourse(eq(1L), eq(10L), any(), any()))
                .thenThrow(new PermissionDeniedException(
                    ErrorStatus.PERMISSION_DENIED_PUBLIC_COURSE_UPDATE_EXCEPTION,
                    ErrorStatus.PERMISSION_DENIED_PUBLIC_COURSE_UPDATE_EXCEPTION.getMessage()));

            mockMvc.perform(withAuth(patch("/api/public-course/10"))
                    .contentType("application/json")
                    .content("{\"title\":\"새 제목\",\"description\":\"새 설명\"}"))
                .andExpect(status().isForbidden());
        }
    }
}
