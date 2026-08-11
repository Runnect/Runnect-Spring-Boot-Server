package org.runnect.server.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import org.runnect.server.config.jwt.JwtService;
import org.runnect.server.config.slack.SlackApi;
import org.runnect.server.user.dto.response.DeleteUserResponseDto;
import org.runnect.server.user.dto.response.MyPageResponseDto;
import org.runnect.server.user.dto.response.UpdateUserNicknameResponseDto;
import org.runnect.server.user.dto.response.UserProfileResponseDto;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.entity.SocialType;
import org.runnect.server.user.exception.userException.NotFoundUserException;
import org.runnect.server.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;
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

    private RunnectUser buildUser(Long id, String nickname) {
        RunnectUser user = RunnectUser.builder()
            .nickname(nickname).socialId("social-" + id).email("user" + id + "@runnect.io")
            .provider(SocialType.KAKAO).build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Nested
    @DisplayName("GET /api/user")
    class GetMyPage {

        @Test
        @DisplayName("정상 요청이면 200과 마이페이지 정보를 반환한다")
        void 정상_조회() throws Exception {
            RunnectUser user = buildUser(1L, "러너1");
            when(userService.getMyPage(1L)).thenReturn(MyPageResponseDto.of(user, 50));

            mockMvc.perform(withAuth(get("/api/user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.id").value(1))
                .andExpect(jsonPath("$.data.user.nickname").value("러너1"));
        }

        @Test
        @DisplayName("존재하지 않는 유저면 404를 반환한다")
        void 존재하지_않는_유저() throws Exception {
            when(userService.getMyPage(1L)).thenThrow(new NotFoundUserException(
                ErrorStatus.NOT_FOUND_USER_EXCEPTION, ErrorStatus.NOT_FOUND_USER_EXCEPTION.getMessage()));

            mockMvc.perform(withAuth(get("/api/user")))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /api/user")
    class UpdateUserNickname {

        @Test
        @DisplayName("정상 요청이면 200과 변경된 닉네임을 반환한다")
        void 정상_변경() throws Exception {
            RunnectUser user = buildUser(1L, "새닉네임");
            when(userService.updateUserNickname(eq(1L), any())).thenReturn(
                UpdateUserNicknameResponseDto.of(user, 50));

            mockMvc.perform(withAuth(patch("/api/user"))
                    .contentType("application/json")
                    .content("{\"nickname\":\"새닉네임\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.nickname").value("새닉네임"));
        }

        @Test
        @DisplayName("닉네임이 빈 값이면 400을 반환한다")
        void 닉네임_빈값() throws Exception {
            mockMvc.perform(withAuth(patch("/api/user"))
                    .contentType("application/json")
                    .content("{\"nickname\":\"\"}"))
                .andExpect(status().isBadRequest());

            BDDMockito.verifyNoInteractions(userService);
        }
    }

    @Nested
    @DisplayName("GET /api/user/{profileUserId}")
    class GetUserProfile {

        @Test
        @DisplayName("정상 요청이면 200과 프로필 정보를 반환한다")
        void 정상_조회() throws Exception {
            RunnectUser profileUser = buildUser(2L, "상대유저");
            UserProfileResponseDto response = UserProfileResponseDto.of(
                UserProfileResponseDto.UserProfile.of(profileUser, 30), Collections.emptyList());
            when(userService.getUserProfile(2L, 1L)).thenReturn(response);

            mockMvc.perform(withAuth(get("/api/user/2")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.userId").value(2))
                .andExpect(jsonPath("$.data.user.nickname").value("상대유저"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/user")
    class DeleteUser {

        @Test
        @DisplayName("정상 요청이면 200과 삭제된 유저 id를 반환한다")
        void 정상_삭제() throws Exception {
            when(userService.deleteUser(1L, "apple-token")).thenReturn(DeleteUserResponseDto.of(1L));

            mockMvc.perform(withAuth(delete("/api/user")).header("appleAccessToken", "apple-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deletedUserId").value(1));
        }

        @Test
        @DisplayName("appleAccessToken 헤더가 없어도 400 없이 서비스에 null로 위임된다")
        void 애플토큰_헤더_없음() throws Exception {
            when(userService.deleteUser(1L, null)).thenReturn(DeleteUserResponseDto.of(1L));

            mockMvc.perform(withAuth(delete("/api/user")))
                .andExpect(status().isOk());
        }
    }
}
