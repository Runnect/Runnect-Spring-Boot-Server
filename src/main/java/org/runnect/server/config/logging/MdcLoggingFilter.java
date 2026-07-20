package org.runnect.server.config.logging;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * 요청마다 traceId를 발급해 MDC에 담아둔다.
 * 로그 패턴에 %X{traceId}를 포함시키면(logback-spring.xml), 여러 요청이 뒤섞인 로그에서도
 * 같은 traceId로 특정 요청의 흐름만 추적할 수 있다.
 */
@Component
public class MdcLoggingFilter implements Filter {

    private static final String TRACE_ID_KEY = "traceId";
    private static final int TRACE_ID_LENGTH = 8;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String traceId = UUID.randomUUID().toString().substring(0, TRACE_ID_LENGTH);
        try {
            MDC.put(TRACE_ID_KEY, traceId);
            chain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID_KEY);
        }
    }
}
