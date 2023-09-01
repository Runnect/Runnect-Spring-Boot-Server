package org.runnect.server.common.advice;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.runnect.server.common.dto.ApiResponseDto;
import org.runnect.server.common.exception.ErrorStatus;
import org.runnect.server.common.exception.BasicException;
import org.runnect.server.config.slack.SlackApi;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Objects;

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

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ApiResponseDto handleMissingRequestParameterException(final MissingServletRequestParameterException e) {
        return ApiResponseDto.error(ErrorStatus.VALIDATION_REQUEST_PARAMETER_MISSING_EXCEPTION, String.format("%s. (%s)", ErrorStatus.VALIDATION_REQUEST_PARAMETER_MISSING_EXCEPTION.getMessage(), e.getParameterName()));
    }

    /**
     * 500 Internal Server Error
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    protected ApiResponseDto<Object> handleException(final Exception error, final HttpServletRequest request) throws IOException {
//        slackApi.sendAlert(error, request);
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


