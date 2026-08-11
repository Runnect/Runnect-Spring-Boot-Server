package org.runnect.server.common.resolver.userId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.runnect.server.common.constant.TokenStatus;
import org.runnect.server.config.jwt.JwtService;
import org.runnect.server.user.exception.authException.InvalidAccessTokenException;
import org.runnect.server.user.exception.authException.NullAccessTokenException;
import org.runnect.server.user.exception.authException.TimeExpiredAccessTokenException;
import org.runnect.server.user.exception.userException.NotFoundUserException;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.NativeWebRequest;

class UserIdResolverTest {

    private static final Long VISITOR_ID = 0L;

    private JwtService jwtService;
    private UserIdResolver userIdResolver;
    private MethodParameter methodParameter;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        userIdResolver = new UserIdResolver(jwtService);
        ReflectionTestUtils.setField(userIdResolver, "VISITOR_ID", VISITOR_ID);
        ReflectionTestUtils.invokeMethod(userIdResolver, "setVISITOR_POSSIBLE_URLS", "/api/public-course");
        methodParameter = mock(MethodParameter.class);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    private NativeWebRequest webRequestWith(String accessToken, String refreshToken, String method, String uri) {
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getHeader("accessToken")).thenReturn(accessToken);
        when(servletRequest.getHeader("refreshToken")).thenReturn(refreshToken);
        when(servletRequest.getMethod()).thenReturn(method);
        when(servletRequest.getRequestURI()).thenReturn(uri);

        NativeWebRequest webRequest = mock(NativeWebRequest.class);
        when(webRequest.getNativeRequest()).thenReturn(servletRequest);
        return webRequest;
    }

    @Test
    void accessToken이_없으면_예외를_던진다() {
        NativeWebRequest webRequest = webRequestWith(null, "refresh", "GET", "/api/user");

        assertThatThrownBy(() -> userIdResolver.resolveArgument(methodParameter, null, webRequest, null))
                .isInstanceOf(NullAccessTokenException.class);
    }

    @Test
    void refreshToken이_없으면_예외를_던진다() {
        NativeWebRequest webRequest = webRequestWith("access", null, "GET", "/api/user");

        assertThatThrownBy(() -> userIdResolver.resolveArgument(methodParameter, null, webRequest, null))
                .isInstanceOf(NullAccessTokenException.class);
    }

    @Test
    void 방문자_모드_허용_URL이면_VISITOR_ID를_반환하고_MDC에_채운다() {
        NativeWebRequest webRequest = webRequestWith("visitor", "visitor", "GET", "/api/public-course/123");

        Object result = userIdResolver.resolveArgument(methodParameter, null, webRequest, null);

        assertThat(result).isEqualTo(VISITOR_ID);
        assertThat(MDC.get("userId")).isEqualTo(String.valueOf(VISITOR_ID));
    }

    @Test
    void 만료된_토큰이면_예외를_던진다() {
        when(jwtService.verifyToken("expired")).thenReturn(TokenStatus.TOKEN_EXPIRED);
        NativeWebRequest webRequest = webRequestWith("expired", "refresh", "GET", "/api/user");

        assertThatThrownBy(() -> userIdResolver.resolveArgument(methodParameter, null, webRequest, null))
                .isInstanceOf(TimeExpiredAccessTokenException.class);
    }

    @Test
    void 유효하지_않은_토큰이면_예외를_던진다() {
        when(jwtService.verifyToken("invalid")).thenReturn(TokenStatus.TOKEN_INVALID);
        NativeWebRequest webRequest = webRequestWith("invalid", "refresh", "GET", "/api/user");

        assertThatThrownBy(() -> userIdResolver.resolveArgument(methodParameter, null, webRequest, null))
                .isInstanceOf(InvalidAccessTokenException.class);
    }

    @Test
    void 유효한_토큰이면_userId를_반환하고_MDC에_채운다() {
        when(jwtService.verifyToken("valid")).thenReturn(TokenStatus.TOKEN_VALID);
        when(jwtService.getJwtContents("valid")).thenReturn("42");
        NativeWebRequest webRequest = webRequestWith("valid", "refresh", "GET", "/api/user");

        Object result = userIdResolver.resolveArgument(methodParameter, null, webRequest, null);

        assertThat(result).isEqualTo(42L);
        assertThat(MDC.get("userId")).isEqualTo("42");
    }

    @Test
    void 토큰의_userId_클레임이_숫자가_아니면_예외를_던진다() {
        when(jwtService.verifyToken("valid")).thenReturn(TokenStatus.TOKEN_VALID);
        when(jwtService.getJwtContents("valid")).thenReturn("not-a-number");
        NativeWebRequest webRequest = webRequestWith("valid", "refresh", "GET", "/api/user");

        assertThatThrownBy(() -> userIdResolver.resolveArgument(methodParameter, null, webRequest, null))
                .isInstanceOf(NotFoundUserException.class);
    }

    @Test
    void UserId_애노테이션과_Long_타입일_때만_지원한다() {
        when(methodParameter.hasParameterAnnotation(UserId.class)).thenReturn(true);
        when(methodParameter.getParameterType()).thenReturn((Class) Long.class);

        assertThat(userIdResolver.supportsParameter(methodParameter)).isTrue();
    }
}
