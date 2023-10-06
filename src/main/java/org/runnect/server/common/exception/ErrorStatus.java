package org.runnect.server.common.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ErrorStatus {

    /**
     * 400 BAD REQUEST
     */
    REQUEST_VALIDATION_EXCEPTION(HttpStatus.BAD_REQUEST, "잘못된 요청입니다"),
    VALIDATION_REQUEST_MISSING_EXCEPTION(HttpStatus.BAD_REQUEST, "요청값이 입력되지 않았습니다."),
    VALIDATION_REQUEST_HEADER_MISSING_EXCEPTION(HttpStatus.BAD_REQUEST, "요청 헤더값이 입력되지 않았습니다."),
    VALIDATION_REQUEST_PARAMETER_MISSING_EXCEPTION(HttpStatus.BAD_REQUEST, "요청 파라미터값이 입력되지 않았습니다."),
    NULL_ACCESS_TOKEN_EXCEPTION(HttpStatus.BAD_REQUEST, "토큰 값이 없습니다."),
    NOT_FOUND_COURSE_EXCEPTION(HttpStatus.BAD_REQUEST, "유효하지 않은 코스 아이디"),
    NO_RECORD_TITLE(HttpStatus.BAD_REQUEST, "경로 타이틀 없음"),
    NO_RECORD_TIME(HttpStatus.BAD_REQUEST, "경로 뛴 시간 없음"),
    NO_RECORD_PACE(HttpStatus.BAD_REQUEST, "경로 뛴 페이스 없음"),
    VALIDATION_COURSE_PATH_EXCEPTION(HttpStatus.BAD_REQUEST, "잘못된 path 값이 입력되었습니다."),
    NOT_FOUND_RECORD_EXCEPTION(HttpStatus.BAD_REQUEST, "존재하지 않는 레코드 아이디입니다."),

    /**
     * 401 UNAUTHORIZED
     */
    TOKEN_TIME_EXPIRED_EXCEPTION(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    INVALID_REFRESH_TOKEN_EXCEPTION(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    INVALID_ACCESS_TOKEN_EXCEPTION(HttpStatus.UNAUTHORIZED, "유효하지 않은 엑세스 토큰입니다."),
    INVALID_GOOGLE_ID_TOKEN_EXCEPTION(HttpStatus.UNAUTHORIZED, "유효하지 않은 구글 아이디 토큰입니다."),

    /**
     * 403 FORBIDDEN
     */
    PERMISSION_DENIED_PUBLIC_COURSE_DELETE_EXCEPTION(HttpStatus.FORBIDDEN, "퍼블릭 코스를 삭제할 권한이 존재하지 않습니다."),
    PERMISSION_DENIED_RECORD_DELETE_EXCEPTION(HttpStatus.FORBIDDEN, "기록을 삭제할 권한이 존재하지 않습니다."),

    /**
     * 404 NOT FOUND
     */
    NOT_FOUND_USER_EXCEPTION(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다"),
    NOT_FOUND_IMAGE_EXCEPTION(HttpStatus.BAD_REQUEST, "잘못된 이미지 파일입니다"),
    NOT_FOUND_PUBLICCOURSE_EXCEPTION(HttpStatus.BAD_REQUEST, "존재하지 않는 public course id입니다."),
    NOT_FOUND_SCRAP_EXCEPTION(HttpStatus.BAD_REQUEST, "스크랩한 코스가 없습니다."),

    /**
     * 409 CONFLICT
     */
    ALREADY_EXIST_USER_EXCEPTION(HttpStatus.CONFLICT, "이미 존재하는 유저입니다"),
    ALREADY_EXIST_NICKNAME_EXCEPTION(HttpStatus.CONFLICT, "중복된 닉네임입니다."),

    /**
     * 500 INTERNAL SERVER ERROR
     */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 서버 에러가 발생했습니다"),
    PATH_CONVERT_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "path 변환 과정에서 오류가 발생했습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getHttpStatusCode() {
        return httpStatus.value();
    }
}
