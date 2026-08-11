package org.runnect.server.course.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.runnect.server.common.constant.TokenStatus;
import org.runnect.server.common.exception.NotFoundException;
import org.runnect.server.common.module.convert.CoordinateDto;
import org.runnect.server.config.jwt.JwtService;
import org.runnect.server.config.slack.SlackApi;
import org.runnect.server.course.dto.request.CourseCreateRequestDto;
import org.runnect.server.course.dto.request.DeleteCoursesRequestDto;
import org.runnect.server.course.dto.response.CourseCreateResponseDto;
import org.runnect.server.course.dto.response.CourseGetByUserResponseDto;
import org.runnect.server.course.dto.response.DeleteCoursesResponseDto;
import org.runnect.server.course.dto.response.UpdateCourseResponseDto;
import org.runnect.server.course.dto.response.UserResponse;
import org.runnect.server.course.service.CourseService;
import org.runnect.server.external.aws.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 컨트롤러 레이어(라우팅, @Valid 검증, @UserId 인증 리졸버, 예외→HTTP 상태코드 매핑)를
 * 서비스 레이어 단위 테스트와는 별개로 검증한다. CourseService/S3Service는 모킹하고
 * 나머지(WebConfig, UserIdResolver, ControllerExceptionAdvice)는 실제 빈을 그대로 쓴다.
 */
@WebMvcTest(CourseController.class)
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CourseService courseService;
    @MockBean
    private S3Service s3Service;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private SlackApi slackApi;

    @BeforeEach
    void setUpAuth() {
        BDDMockito.lenient().when(jwtService.verifyToken("valid")).thenReturn(TokenStatus.TOKEN_VALID);
        BDDMockito.lenient().when(jwtService.getJwtContents("valid")).thenReturn("1");
    }

    private <B extends org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder> B withAuth(
        B builder) {
        builder.header("accessToken", "valid").header("refreshToken", "valid");
        return builder;
    }

    private CourseCreateRequestDto validCreateRequest() {
        return new CourseCreateRequestDto(
            Arrays.asList(new CoordinateDto(37.5665, 126.9780), new CoordinateDto(37.5651, 126.9895)),
            "정왕역 코스", 5.2f, "정왕역", "경기 시흥시 정왕동");
    }

    @Nested
    @DisplayName("POST /api/course")
    class CreateCourse {

        @Test
        @DisplayName("정상 요청이면 201과 생성된 코스 정보를 반환한다")
        void 정상_생성() throws Exception {
            when(s3Service.uploadImage(any(), eq("course"))).thenReturn("https://image.example/course.png");
            when(courseService.createCourse(eq(1L), any(), any()))
                .thenReturn(CourseCreateResponseDto.of(100L, LocalDateTime.of(2026, 1, 1, 0, 0)));

            MockMultipartFile data = new MockMultipartFile("data", "", "application/json",
                objectMapper.writeValueAsBytes(validCreateRequest()));
            MockMultipartFile image = new MockMultipartFile("image", "photo.jpg", "image/jpeg", "content".getBytes());

            mockMvc.perform(withAuth(multipart("/api/course")).file(data).file(image))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(100));
        }

        @Test
        @DisplayName("인증 헤더가 없으면 400과 함께 요청이 서비스까지 도달하지 않는다")
        void 인증헤더_없음() throws Exception {
            MockMultipartFile data = new MockMultipartFile("data", "", "application/json",
                objectMapper.writeValueAsBytes(validCreateRequest()));
            MockMultipartFile image = new MockMultipartFile("image", "photo.jpg", "image/jpeg", "content".getBytes());

            mockMvc.perform(multipart("/api/course").file(data).file(image))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

            BDDMockito.verifyNoInteractions(courseService);
        }

        @Test
        @DisplayName("title이 비어있으면 유효성 검증에서 400을 반환한다")
        void 유효성검증_실패() throws Exception {
            CourseCreateRequestDto invalid = new CourseCreateRequestDto(
                Arrays.asList(new CoordinateDto(37.5665, 126.9780), new CoordinateDto(37.5651, 126.9895)),
                "", 5.2f, "정왕역", "경기 시흥시 정왕동");
            MockMultipartFile data = new MockMultipartFile("data", "", "application/json",
                objectMapper.writeValueAsBytes(invalid));
            MockMultipartFile image = new MockMultipartFile("image", "photo.jpg", "image/jpeg", "content".getBytes());

            mockMvc.perform(withAuth(multipart("/api/course")).file(data).file(image))
                .andExpect(status().isBadRequest());

            BDDMockito.verifyNoInteractions(courseService);
        }
    }

    @Nested
    @DisplayName("GET /api/course/user")
    class GetCourseByUser {

        @Test
        @DisplayName("정상 요청이면 200과 코스 목록을 반환한다")
        void 정상_조회() throws Exception {
            when(courseService.getCourseByUser(1L)).thenReturn(
                CourseGetByUserResponseDto.of(UserResponse.of(1L), Collections.emptyList()));

            mockMvc.perform(withAuth(get("/api/course/user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.id").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/course/detail/{courseId}")
    class GetCourseDetail {

        @Test
        @DisplayName("서비스에서 NotFoundException이 나면 400으로 매핑된다")
        void 존재하지_않는_코스() throws Exception {
            when(courseService.getCourseDetail(anyLong(), eq(1L)))
                .thenThrow(new NotFoundException(
                    org.runnect.server.common.constant.ErrorStatus.NOT_FOUND_COURSE_EXCEPTION,
                    org.runnect.server.common.constant.ErrorStatus.NOT_FOUND_COURSE_EXCEPTION.getMessage()));

            mockMvc.perform(withAuth(get("/api/course/detail/999")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("PATCH /api/course/{courseId}")
    class UpdateCourse {

        @Test
        @DisplayName("정상 요청이면 200과 수정된 제목을 반환한다")
        void 정상_수정() throws Exception {
            when(courseService.updateCourse(eq(1L), eq(10L), eq("새 제목")))
                .thenReturn(UpdateCourseResponseDto.of(courseWithTitle(10L, "새 제목")));

            mockMvc.perform(withAuth(patch("/api/course/10"))
                    .contentType("application/json")
                    .content("{\"title\":\"새 제목\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.course.title").value("새 제목"));
        }

        @Test
        @DisplayName("title이 비어있으면 400을 반환한다")
        void 빈_제목() throws Exception {
            mockMvc.perform(withAuth(patch("/api/course/10"))
                    .contentType("application/json")
                    .content("{\"title\":\"\"}"))
                .andExpect(status().isBadRequest());

            BDDMockito.verifyNoInteractions(courseService);
        }

        private org.runnect.server.course.entity.Course courseWithTitle(Long id, String title) {
            org.runnect.server.course.entity.Course course = org.runnect.server.course.entity.Course.builder()
                .title(title)
                .departureRegion("경기").departureCity("시흥시").departureTown("정왕동")
                .distance(5.2f).image("image.png")
                .path(org.runnect.server.common.module.convert.CoordinatePathConverter.coorConvertPath(
                    Arrays.asList(new CoordinateDto(37.5665, 126.9780), new CoordinateDto(37.5651, 126.9895))))
                .build();
            org.springframework.test.util.ReflectionTestUtils.setField(course, "id", id);
            return course;
        }
    }

    @Nested
    @DisplayName("PUT /api/course")
    class DeleteCourses {

        @Test
        @DisplayName("정상 요청이면 200과 삭제 개수를 반환한다")
        void 정상_삭제() throws Exception {
            when(courseService.deleteCourses(Collections.singletonList(10L), 1L))
                .thenReturn(DeleteCoursesResponseDto.from(1));

            mockMvc.perform(withAuth(put("/api/course"))
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(
                        new DeleteCoursesRequestDto(Collections.singletonList(10L)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deletedCourseCount").value(1));
        }
    }
}
