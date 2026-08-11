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
}
