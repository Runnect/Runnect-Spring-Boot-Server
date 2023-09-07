package org.runnect.server.common.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum SuccessStatus {
    /**
     * 200 OK
     */
    LOGIN_SUCCESS(HttpStatus.OK, "로그인에 성공했습니다."),
    NEW_TOKEN_SUCCESS(HttpStatus.OK, "토큰 재발급에 성공했습니다."),
    CREATE_SCRAP_SUCCESS(HttpStatus.OK, "코스 스크랩 성공"),
    DELETE_SCRAP_SUCCESS(HttpStatus.OK, "코스 스크랩 취소 성공"),
    READ_RECORD_SUCCESS(HttpStatus.OK, "활동 기록 조회 성공"),
    GET_COURSE_LIST_BY_USER(HttpStatus.OK, "내가 그린 코스 리스트 조회에 성공했습니다."),

    /**
     * 201 CREATED
     */
    SIGNUP_SUCCESS(HttpStatus.CREATED, "회원가입이 완료됐습니다."),
    CREATE_RECORD_SUCCESS(HttpStatus.CREATED, "경로기록하기 성공"),
    CREATE_COURSE_SUCCESS(HttpStatus.CREATED, "코스 생성에 성공했습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getHttpStatusCode() {
        return httpStatus.value();
    }
}
