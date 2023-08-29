package org.runnect.server.common.resolver.userId;

import org.runnect.server.common.exception.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.runnect.server.user.exception.authException.InvalidAccessTokenException;
import org.runnect.server.user.exception.authException.NullAccessTokenException;
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
        String header = request.getHeader("Authorization");

        if (header == null) {
            throw new NullAccessTokenException(ErrorStatus.NULL_ACCESS_TOKEN_EXCEPTION, ErrorStatus.NULL_ACCESS_TOKEN_EXCEPTION.getMessage());
        }

        final String token = header.substring(7);        //배리어 타입으로 토큰을 받기때문에 앞의 'Bearer ' 없애기

        // 토큰 검증
        if (!jwtService.verifyToken(token)) {
            throw new InvalidAccessTokenException(ErrorStatus.INVALID_ACCESS_TOKEN_EXCEPTION, ErrorStatus.INVALID_ACCESS_TOKEN_EXCEPTION.getMessage());
        }

        // 유저 아이디 반환
        final String tokenContents = jwtService.getJwtContents(token);

        try {
            return Long.parseLong(tokenContents);
        } catch (NumberFormatException e) {
            throw new NotFoundUserException(ErrorStatus.NOT_FOUND_USER_EXCEPTION, ErrorStatus.NOT_FOUND_USER_EXCEPTION.getMessage());
        }

    }
}