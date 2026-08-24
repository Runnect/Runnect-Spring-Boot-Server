package org.runnect.server.auth.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.runnect.server.auth.dto.response.GetNewTokenResponseDto;
import org.runnect.server.auth.dto.response.SignInResponseDto;
import org.runnect.server.auth.dto.response.SignUpResponseDto;
import org.runnect.server.auth.service.AuthService;
import org.runnect.server.common.constant.ErrorStatus;
import org.runnect.server.config.jwt.JwtService;
import org.runnect.server.config.slack.SlackApi;
import org.runnect.server.user.exception.authException.InvalidRefreshTokenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private SlackApi slackApi;

    @Nested
    @DisplayName("POST /api/auth")
    class SignIn {

        @Test
        @DisplayName("기존 유저면 200과 LOGIN 응답을 반환한다")
        void 기존_유저_로그인() throws Exception {
            when(authService.signIn(BDDMockito.any())).thenReturn(
                SignInResponseDto.of("KAKAO", "user@runnect.io", "access-token", "refresh-token"));

            mockMvc.perform(post("/api/auth")
                    .contentType("application/json")
                    .content("{\"token\":\"kakao-token\",\"provider\":\"kakao\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("user@runnect.io"));
        }

        @Test
        @DisplayName("신규 유저면 200과 SIGNUP 응답을 반환한다")
        void 신규_유저_회원가입() throws Exception {
            when(authService.signIn(BDDMockito.any())).thenReturn(
                SignUpResponseDto.of("KAKAO", "new@runnect.io", "러너1", "access-token", "refresh-token"));

            mockMvc.perform(post("/api/auth")
                    .contentType("application/json")
                    .content("{\"token\":\"kakao-token\",\"provider\":\"kakao\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("러너1"));
        }

        @Test
        @DisplayName("token이 없으면 400을 반환한다")
        void 토큰_없음() throws Exception {
            mockMvc.perform(post("/api/auth")
                    .contentType("application/json")
                    .content("{\"provider\":\"kakao\"}"))
                .andExpect(status().isBadRequest());

            BDDMockito.verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("provider가 없으면 400을 반환한다")
        void 프로바이더_없음() throws Exception {
            mockMvc.perform(post("/api/auth")
                    .contentType("application/json")
                    .content("{\"token\":\"kakao-token\"}"))
                .andExpect(status().isBadRequest());

            BDDMockito.verifyNoInteractions(authService);
        }
    }

    @Nested
    @DisplayName("GET /api/auth/getNewToken")
    class GetNewToken {

        @Test
        @DisplayName("정상 요청이면 200과 재발급된 토큰을 반환한다")
        void 정상_재발급() throws Exception {
            when(authService.getNewToken("old-access", "valid-refresh"))
                .thenReturn(GetNewTokenResponseDto.of("new-access", "new-refresh"));

            mockMvc.perform(get("/api/auth/getNewToken")
                    .header("accessToken", "old-access")
                    .header("refreshToken", "valid-refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("new-access"));
        }

        @Test
        @DisplayName("refreshToken 헤더가 없으면 400을 반환한다")
        void 리프레시토큰_헤더_없음() throws Exception {
            mockMvc.perform(get("/api/auth/getNewToken").header("accessToken", "old-access"))
                .andExpect(status().isBadRequest());

            BDDMockito.verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("refreshToken이 빈 문자열이면 400을 반환한다")
        void 리프레시토큰_빈문자열() throws Exception {
            mockMvc.perform(get("/api/auth/getNewToken")
                    .header("accessToken", "old-access")
                    .header("refreshToken", ""))
                .andExpect(status().isBadRequest());

            BDDMockito.verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("저장된 리프레시 토큰과 일치하지 않으면 401을 반환한다")
        void 리프레시토큰_불일치() throws Exception {
            when(authService.getNewToken("old-access", "wrong-refresh"))
                .thenThrow(new InvalidRefreshTokenException(
                    ErrorStatus.INVALID_REFRESH_TOKEN_EXCEPTION,
                    ErrorStatus.INVALID_REFRESH_TOKEN_EXCEPTION.getMessage()));

            mockMvc.perform(get("/api/auth/getNewToken")
                    .header("accessToken", "old-access")
                    .header("refreshToken", "wrong-refresh"))
                .andExpect(status().isUnauthorized());
        }
    }

    // prod Grafana 로그 확인 결과, 최근 7일간 5xx 40건이 전부 정상 트래픽이 아니라
    // Postman 등으로 잘못된 메서드/형식을 직접 찌른 요청(GET /api/auth, 잘못된
    // Content-Type의 POST /api/auth)이었다. 원래는 405/415로 응답해야 할 요청인데
    // ControllerExceptionAdvice의 범용 Exception 핸들러가 이걸 500으로 뭉개고
    // Slack/Sentry 알림까지 매번 보내고 있었다 — 진짜 장애가 아닌데 노이즈만 쌓이는 구조.
    @Nested
    @DisplayName("잘못된 형태의 요청 (클라이언트 오류, 서버 장애 아님)")
    class InvalidRequestShape {

        @Test
        @DisplayName("POST만 지원하는 /api/auth에 GET으로 요청하면 405를 반환하고 Slack 알림을 보내지 않는다")
        void 지원하지_않는_메서드() throws Exception {
            mockMvc.perform(get("/api/auth"))
                .andExpect(status().isMethodNotAllowed());

            BDDMockito.verifyNoInteractions(slackApi);
        }

        @Test
        @DisplayName("JSON을 기대하는 /api/auth에 multipart로 요청하면 415를 반환하고 Slack 알림을 보내지 않는다")
        void 지원하지_않는_컨텐츠타입() throws Exception {
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                    .multipart("/api/auth")
                    .file("token", "kakao-token".getBytes()))
                .andExpect(status().isUnsupportedMediaType());

            BDDMockito.verifyNoInteractions(slackApi);
        }
    }
}
