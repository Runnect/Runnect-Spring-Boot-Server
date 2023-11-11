package org.runnect.server.common.constant;

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
    GET_RECORD_SUCCESS(HttpStatus.OK, "활동 기록 조회 성공"),
    GET_COURSE_LIST_BY_USER_SUCCESS(HttpStatus.OK, "내가 그린 코스 리스트 조회에 성공했습니다."),
    GET_SCRAP_COURSE_BY_USER_SUCCESS(HttpStatus.OK, "스크랩한 코스 조회 성공"),
    UPDATE_RECORD_SUCCESS(HttpStatus.OK, "활동 기록 수정 성공"),
    GET_COURSE_DETAIL_SUCCESS(HttpStatus.OK, "코스 상세 조회에 성공했습니다."),
    GET_MY_PAGE_SUCCESS(HttpStatus.OK, "마이페이지 조회에 성공했습니다."),
    UPDATE_USER_NICKNAME_SUCCESS(HttpStatus.OK, "닉네임 변경에 성공했습니다."),
    UPDATE_COURSE_SUCCESS(HttpStatus.OK, "내가 그린 코스 제목 수정 성공"),
    GET_USER_PROFILE_SUCCESS(HttpStatus.OK, "유저 프로필 조회에 성공했습니다."),
    DELETE_PUBLIC_COURSE_SUCCESS(HttpStatus.OK, "퍼블릭 코스 삭제에 성공했습니다."),
    DELETE_RECORD_SUCCESS(HttpStatus.OK, "기록 삭제에 성공했습니다."),
    UPDATE_PUBLIC_COURSE_SUCCESS(HttpStatus.OK, "업로드한 코스 수정 성공"),
    GET_PUBLIC_COURSE_BY_USER_SUCCESS(HttpStatus.OK,"유저가 업로드한 코스 조회 성공"),
    DELETE_COURSES_SUCCESS(HttpStatus.OK, "코스 삭제 성공"),
    GET_USER_STAMPS_SUCCESS(HttpStatus.OK, "유저 스탬프 조회 성공"),
    GET_PUBLIC_COURSE_TOTAL_PAGE_COUNT_SUCCESS(HttpStatus.OK,"추천 코스 전체 페이지 개수 조회 성공"),
    GET_RECOMMENDED_PUBLIC_COURSE_SUCCESS(HttpStatus.OK, "추천 코스 조회 성공"),
    SEARCH_PUBLIC_COURSE_SUCCESS(HttpStatus.OK,"업로드된 코스 검색 성공"),
    GET_PUBLIC_COURSE_DETAIL_SUCCESS(HttpStatus.OK,"업로드 코스 상세 조회 성공"),
    GET_MARATHON_PUBLIC_COURSE_SUCCESS(HttpStatus.OK,"마라톤 코스 조회 성공"),

    /**
     * 201 CREATED
     */
    SIGNUP_SUCCESS(HttpStatus.CREATED, "회원가입이 완료됐습니다."),
    CREATE_RECORD_SUCCESS(HttpStatus.CREATED, "경로기록하기 성공"),
    CREATE_COURSE_SUCCESS(HttpStatus.CREATED, "코스 생성에 성공했습니다."),
    CREATE_PUBLIC_COURSE_SUCCESS(HttpStatus.CREATED, "코드 업로드에 성공했습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getHttpStatusCode() {
        return httpStatus.value();
    }
}
