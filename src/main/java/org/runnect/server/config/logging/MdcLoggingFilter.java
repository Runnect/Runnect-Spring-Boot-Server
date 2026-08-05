package org.runnect.server.config.logging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.runnect.server.common.constant.TokenStatus;
import org.runnect.server.config.jwt.JwtService;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

/**
 * 요청마다 traceId를 발급해 MDC에 담아둔다.
 * 로그 패턴에 %X{traceId}를 포함시키면(logback-spring.xml), 여러 요청이 뒤섞인 로그에서도
 * 같은 traceId로 특정 요청의 흐름만 추적할 수 있다.
 *
 * userId는 @UserId 파라미터가 없는 요청(토큰 재발급, 배너 등)에서도 로그에 남도록
 * 여기서 best-effort로 한 번 더 채운다. 실제 인증 검증/방문자 모드 처리는 UserIdResolver가
 * 맡고, 그 결과가 이후 이 값을 덮어쓴다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MdcLoggingFilter implements Filter {

    private static final String TRACE_ID_KEY = "traceId";
    private static final int TRACE_ID_LENGTH = 8;
    private static final String USER_ID_KEY = "userId";

    private final JwtService jwtService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String traceId = UUID.randomUUID().toString().substring(0, TRACE_ID_LENGTH);
        try {
            MDC.put(TRACE_ID_KEY, traceId);
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            populateUserIdBestEffort(httpRequest);
            log.info("{} {}", httpRequest.getMethod(), httpRequest.getRequestURI());
            chain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID_KEY);
            MDC.remove(USER_ID_KEY);
        }
    }

    private void populateUserIdBestEffort(HttpServletRequest request) {
        String accessToken = request.getHeader("accessToken");
        if (accessToken == null) {
            return;
        }
        try {
            if (jwtService.verifyToken(accessToken) == TokenStatus.TOKEN_VALID) {
                MDC.put(USER_ID_KEY, jwtService.getJwtContents(accessToken));
            }
        } catch (RuntimeException e) {
            // 로깅 목적의 best-effort 파싱이므로 실패해도 요청 처리는 계속 진행한다.
        }
    }
}
