package org.runnect.server.common.resolver.userId;

import org.runnect.server.common.constant.TokenStatus;
import org.runnect.server.common.constant.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.runnect.server.user.exception.authException.InvalidAccessTokenException;
import org.runnect.server.user.exception.authException.NullAccessTokenException;
import org.runnect.server.user.exception.authException.TimeExpiredAccessTokenException;
import org.runnect.server.user.exception.userException.NotFoundUserException;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.runnect.server.config.jwt.JwtService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

@RequiredArgsConstructor
@Component
public class UserIdResolver implements HandlerMethodArgumentResolver {

    private final JwtService jwtService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(UserId.class) && Long.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(@NotNull MethodParameter parameter, ModelAndViewContainer modelAndViewContainer, @NotNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

        final HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String accessToken = request.getHeader("accessToken");
        String refreshToken = request.getHeader("refreshToken");

        if (accessToken == null || refreshToken == null) {
            throw new NullAccessTokenException(ErrorStatus.NULL_ACCESS_TOKEN_EXCEPTION, ErrorStatus.NULL_ACCESS_TOKEN_EXCEPTION.getMessage());
        }


        // 토큰 검증
        if (jwtService.verifyToken(accessToken) == TokenStatus.TOKEN_EXPIRED) {
            throw new TimeExpiredAccessTokenException(ErrorStatus.ACCESS_TOKEN_TIME_EXPIRED_EXCEPTION, ErrorStatus.ACCESS_TOKEN_TIME_EXPIRED_EXCEPTION.getMessage());
        } else if (jwtService.verifyToken(accessToken) == TokenStatus.TOKEN_INVALID) {
            throw new InvalidAccessTokenException(ErrorStatus.INVALID_ACCESS_TOKEN_EXCEPTION, ErrorStatus.INVALID_ACCESS_TOKEN_EXCEPTION.getMessage());
        }


        // 유저 아이디 반환
        final String tokenContents = jwtService.getJwtContents(accessToken);

        try {
            return Long.parseLong(tokenContents);
        } catch (NumberFormatException e) {
            throw new NotFoundUserException(ErrorStatus.NOT_FOUND_USER_EXCEPTION, ErrorStatus.NOT_FOUND_USER_EXCEPTION.getMessage());
        }

    }
}
