package org.runnect.server.user.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.runnect.server.common.constant.TokenStatus;
import org.runnect.server.config.jwt.JwtService;
import org.runnect.server.config.slack.SlackApi;
import org.runnect.server.user.dto.response.GetUserStampsResponseDto;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.entity.SocialType;
import org.runnect.server.user.service.UserStampService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(UserStampController.class)
class UserStampControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserStampService userStampService;
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

    @Test
    @DisplayName("GET /api/stamp/user - 정상 요청이면 200과 유저 스탬프 목록을 반환한다")
    void 정상_조회() throws Exception {
        RunnectUser user = RunnectUser.builder()
            .nickname("러너1").socialId("social-1").email("user1@runnect.io")
            .provider(SocialType.KAKAO).build();
        ReflectionTestUtils.setField(user, "id", 1L);
        when(userStampService.findUserStamps(1L)).thenReturn(GetUserStampsResponseDto.from(user));

        mockMvc.perform(withAuth(get("/api/stamp/user")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.user.id").value(1))
            .andExpect(jsonPath("$.data.stamps").isEmpty());
    }
}
