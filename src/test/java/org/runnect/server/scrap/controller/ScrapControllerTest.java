package org.runnect.server.scrap.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.runnect.server.common.constant.TokenStatus;
import org.runnect.server.config.jwt.JwtService;
import org.runnect.server.config.slack.SlackApi;
import org.runnect.server.scrap.dto.response.CreateAndDeleteScrapResponseDto;
import org.runnect.server.scrap.dto.response.GetScrapCourseResponseDto;
import org.runnect.server.scrap.dto.response.UserResponse;
import org.runnect.server.scrap.service.ScrapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(ScrapController.class)
class ScrapControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScrapService scrapService;
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
    @DisplayName("POST /api/scrap")
    class CreateAndDeleteScrap {

        @Test
        @DisplayName("scrapTF가 true면 200과 스크랩 생성 결과를 반환한다")
        void 스크랩_생성() throws Exception {
            when(scrapService.createAndDeleteScrap(eq(1L), any()))
                .thenReturn(CreateAndDeleteScrapResponseDto.of(10L, 1L, true));

            mockMvc.perform(withAuth(post("/api/scrap"))
                    .contentType("application/json")
                    .content("{\"publicCourseId\":10,\"scrapTF\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.scrapTF").value(true));
        }

        @Test
        @DisplayName("scrapTF가 false면 200과 스크랩 삭제 결과를 반환한다")
        void 스크랩_삭제() throws Exception {
            when(scrapService.createAndDeleteScrap(eq(1L), any()))
                .thenReturn(CreateAndDeleteScrapResponseDto.of(10L, 0L, false));

            mockMvc.perform(withAuth(post("/api/scrap"))
                    .contentType("application/json")
                    .content("{\"publicCourseId\":10,\"scrapTF\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.scrapTF").value(false));
        }

        @Test
        @DisplayName("publicCourseId가 없으면 400을 반환한다")
        void 코스아이디_없음() throws Exception {
            mockMvc.perform(withAuth(post("/api/scrap"))
                    .contentType("application/json")
                    .content("{\"scrapTF\":true}"))
                .andExpect(status().isBadRequest());

            BDDMockito.verifyNoInteractions(scrapService);
        }

        @Test
        @DisplayName("scrapTF가 없으면 400을 반환한다")
        void 스크랩여부_없음() throws Exception {
            mockMvc.perform(withAuth(post("/api/scrap"))
                    .contentType("application/json")
                    .content("{\"publicCourseId\":10}"))
                .andExpect(status().isBadRequest());

            BDDMockito.verifyNoInteractions(scrapService);
        }
    }

    @Nested
    @DisplayName("GET /api/scrap/user")
    class GetScrapCourseByUser {

        @Test
        @DisplayName("정상 요청이면 200과 스크랩 목록을 반환한다")
        void 정상_조회() throws Exception {
            when(scrapService.getScrapCourseByUser(1L))
                .thenReturn(GetScrapCourseResponseDto.of(UserResponse.of(1L), Collections.emptyList()));

            mockMvc.perform(withAuth(get("/api/scrap/user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.userId").value(1));
        }
    }
}
