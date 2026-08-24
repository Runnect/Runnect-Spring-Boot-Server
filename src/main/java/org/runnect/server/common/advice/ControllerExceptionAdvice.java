package org.runnect.server.common.advice;

import io.sentry.Sentry;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.runnect.server.common.constant.ErrorStatus;
import org.runnect.server.common.dto.ApiResponseDto;
import org.runnect.server.common.exception.BasicException;
import org.runnect.server.config.slack.SlackApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@Component
@RequiredArgsConstructor
public class ControllerExceptionAdvice {
    private final SlackApi slackApi;

    /**
     * 400 BAD_REQUEST
     */

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ApiResponseDto handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        FieldError fieldError = Objects.requireNonNull(e.getFieldError());
        return ApiResponseDto.error(ErrorStatus.VALIDATION_REQUEST_MISSING_EXCEPTION, String.format("%s. (%s)", fieldError.getDefaultMessage(), fieldError.getField()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingRequestHeaderException.class)
    protected ApiResponseDto handleMissingRequestHeaderException(final MissingRequestHeaderException e) {
        return ApiResponseDto.error(ErrorStatus.VALIDATION_REQUEST_HEADER_MISSING_EXCEPTION, String.format("%s. (%s)", ErrorStatus.VALIDATION_REQUEST_HEADER_MISSING_EXCEPTION.getMessage(), e.getHeaderName()));
    }

    // validation 실패시 커스텀 에러 response
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    protected ApiResponseDto handleConstraintViolationException(final ConstraintViolationException e) {
        return ApiResponseDto.error(ErrorStatus.VALIDATION_REQUEST_HEADER_MISSING_EXCEPTION, String.format("%s. (%s)", ErrorStatus.VALIDATION_REQUEST_HEADER_MISSING_EXCEPTION.getMessage(), e.getConstraintViolations()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    protected ApiResponseDto handleConstraintViolationException(final IllegalArgumentException e) {
        return ApiResponseDto.error(ErrorStatus.VALIDATION_REQUEST_HEADER_MISSING_EXCEPTION, String.format("%s. (%s)", ErrorStatus.INVALID_PARAMETER_EXCEPTION.getMessage(), e.getMessage()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ApiResponseDto handleMissingRequestParameterException(final MissingServletRequestParameterException e) {
        return ApiResponseDto.error(ErrorStatus.VALIDATION_REQUEST_PARAMETER_MISSING_EXCEPTION, String.format("%s. (%s)", ErrorStatus.VALIDATION_REQUEST_PARAMETER_MISSING_EXCEPTION.getMessage(), e.getParameterName()));
    }



    /**
     * 405 / 415 / 406 — 클라이언트가 잘못된 형태로 요청한 경우 (서버 장애 아님)
     *
     * 실제 prod 로그를 보면 GET /api/auth(POST만 지원), 잘못된 Content-Type의 POST
     * 요청처럼 정상 앱 트래픽이라면 나올 수 없는 요청(Postman 등으로 직접 호출하거나
     * 취약점 스캐너가 찌른 것)이 계속 들어온다. 이런 경우까지 아래 범용 Exception
     * 핸들러가 500으로 뭉개고 Slack/Sentry로 매번 알림을 보내면, 실제 장애가 아닌데도
     * 알림만 계속 쌓인다. 정확한 상태 코드로 응답하고 로그만 WARN으로 남긴다.
     */
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ApiResponseDto handleHttpRequestMethodNotSupportedException(final HttpRequestMethodNotSupportedException e, final HttpServletRequest request) {
        log.warn("[405] {} {} - {}", request.getMethod(), request.getRequestURI(), e.getMessage());
        return ApiResponseDto.error(ErrorStatus.INVALID_HTTP_METHOD_EXCEPTION);
    }

    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    protected ApiResponseDto handleHttpMediaTypeNotSupportedException(final HttpMediaTypeNotSupportedException e, final HttpServletRequest request) {
        log.warn("[415] {} {} - {}", request.getMethod(), request.getRequestURI(), e.getMessage());
        return ApiResponseDto.error(ErrorStatus.UNSUPPORTED_MEDIA_TYPE_EXCEPTION);
    }

    // 클라이언트가 Accept 헤더로 서버가 만들 수 없는 응답 형식을 요구한 경우.
    // 본문(JSON)을 굳이 만들려 하면 그 시도 자체가 다시 협상에 실패해 이중 오류로
    // 이어질 수 있어(handleException 안에서 실패 → DefaultHandlerExceptionResolver로
    // 전파되는 게 실제 로그에서 확인됨), 본문 없이 상태 코드만 응답한다.
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    protected ResponseEntity<Void> handleHttpMediaTypeNotAcceptableException(final HttpMediaTypeNotAcceptableException e, final HttpServletRequest request) {
        log.warn("[406] {} {} - {}", request.getMethod(), request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
    }

    /**
     * 500 Internal Server Error
     *
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    protected ApiResponseDto<Object> handleException(final Exception error, final HttpServletRequest request) {
        log.error("[500 ERROR] {} {}", request.getMethod(), request.getRequestURI(), error);
        try {
            slackApi.sendAlert(error, request);
        } catch (Exception e) {
            log.error("Slack 알림 전송 실패", e);
        }
        try {
            Sentry.captureException(error);
        } catch (Exception e) {
            log.error("Sentry 전송 실패", e);
        }
        return ApiResponseDto.error(ErrorStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * custom error
     */
    @ExceptionHandler(BasicException.class)
    protected ResponseEntity<ApiResponseDto> handleBasicException(BasicException e) {
        return ResponseEntity.status(e.getHttpStatus())
                .body(ApiResponseDto.error(e.getErrorStatus(), e.getMessage()));
    }
}
