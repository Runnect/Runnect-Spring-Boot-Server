package org.runnect.server.common.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.runnect.server.common.exception.ErrorStatus;
import org.runnect.server.common.exception.SuccessStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponseDto<T> {

    private final int status;
    private final Boolean success;
    private final String message;
    private T data;

    public static ApiResponseDto success(SuccessStatus successStatus) {
        return new ApiResponseDto<>(successStatus.getHttpStatusCode(), true, successStatus.getMessage());
    }

    public static <T> ApiResponseDto<T> success(SuccessStatus successStatus, T data) {
        return new ApiResponseDto<T>(successStatus.getHttpStatusCode(), true, successStatus.getMessage(), data);
    }

    public static ApiResponseDto error(ErrorStatus errorStatus) {
        return new ApiResponseDto<>(errorStatus.getHttpStatusCode(), false, errorStatus.getMessage());
    }

    public static ApiResponseDto error(ErrorStatus errorStatus, String message) {
        return new ApiResponseDto<>(errorStatus.getHttpStatusCode(), false, message);
    }
}
