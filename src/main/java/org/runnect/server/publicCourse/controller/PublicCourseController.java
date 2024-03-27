package org.runnect.server.publicCourse.controller;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

import javax.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.runnect.server.common.dto.ApiResponseDto;
import org.runnect.server.common.constant.SuccessStatus;
import org.runnect.server.common.resolver.sort.SortStatusId;
import org.runnect.server.common.resolver.userId.UserId;
import org.runnect.server.publicCourse.dto.request.CreatePublicCourseRequestDto;
import org.runnect.server.publicCourse.dto.request.DeletePublicCoursesRequestDto;
import org.runnect.server.publicCourse.dto.request.UpdatePublicCourseRequestDto;
import org.runnect.server.publicCourse.dto.response.CreatePublicCourseResponseDto;
import org.runnect.server.publicCourse.dto.response.DeletePublicCoursesResponseDto;
import org.runnect.server.publicCourse.dto.response.GetPublicCourseTotalPageCountResponseDto;
import org.runnect.server.publicCourse.dto.response.GetPublicCourseDetailResponseDto;
import org.runnect.server.publicCourse.dto.response.getMarathonPublicCourse.GetMarathonPublicCourseResponseDto;
import org.runnect.server.publicCourse.dto.response.getPublicCourseByUser.GetPublicCourseByUserResponseDto;
import org.runnect.server.publicCourse.dto.response.UpdatePublicCourseResponseDto;
import org.runnect.server.publicCourse.dto.response.recommendPublicCourse.RecommendPublicCourseResponseDto;
import org.runnect.server.publicCourse.dto.response.searchPublicCourse.SearchPublicCourseResponseDto;

import org.runnect.server.publicCourse.service.PublicCourseService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public-course")
@Tag(name = "Public-Course", description = "Public Course API Document")
public class PublicCourseController {

    private final PublicCourseService publicCourseService;


    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "recommendPublicCourse", description = "코스 발견 - 이런 코스는 어때요?")
    public ApiResponseDto<RecommendPublicCourseResponseDto> recommendPublicCourse(
            @UserId final Long userId,
            @RequestParam(required = false) @Positive Integer pageNo,
            @SortStatusId String sort
    ){
        if(pageNo == null){
            pageNo = 1; //basic pageNo
        }
        return ApiResponseDto.success(SuccessStatus.GET_RECOMMENDED_PUBLIC_COURSE_SUCCESS, publicCourseService.recommendPublicCourse(userId, pageNo,sort));

    }


    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "searchPublicCourse", description = "코스 검색")
    public ApiResponseDto<SearchPublicCourseResponseDto> searchPublicCourse(
            @UserId final Long userId,
            @RequestParam @NotBlank String keyword
    ){
        return ApiResponseDto.success(SuccessStatus.SEARCH_PUBLIC_COURSE_SUCCESS,
                publicCourseService.searchPublicCourse(userId, keyword));
    }


    @GetMapping("/marathon")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "getMarathonPublicCourse", description = "코스 발견 - 마라톤 코스")
    public ApiResponseDto<GetMarathonPublicCourseResponseDto> getMarathonPublicCourse(
            @UserId final Long userId
    ){
        return ApiResponseDto.success(SuccessStatus.GET_MARATHON_PUBLIC_COURSE_SUCCESS,
                publicCourseService.getMarathonPublicCourse(userId));
    }



    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "createPublicCourse", description = "내가 그린 코스 업로드 하기")
    public ApiResponseDto<CreatePublicCourseResponseDto> createPublicCourse(
            //TODO : 임시로 개발후 테스트완료하면 다시
            @UserId final Long userId,
            //@RequestHeader final Long userId,
            @Valid @RequestBody final CreatePublicCourseRequestDto createPublicCourseRequestDto
    ){
        return ApiResponseDto.success(SuccessStatus.CREATE_PUBLIC_COURSE_SUCCESS,
                publicCourseService.createPublicCourse(userId, createPublicCourseRequestDto));
    }

    @GetMapping("/detail/{publicCourseId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "getPublicCourseDetail", description = "업로드된 코스 (스크랩 코스, 추천 코스) 상세페이지와 달리기, 러닝기록 작성 뷰")
    public ApiResponseDto<GetPublicCourseDetailResponseDto> getPublicCourseDetail(
            @UserId final Long userId,
            @PathVariable final Long publicCourseId
    ){

        return ApiResponseDto.success(SuccessStatus.GET_PUBLIC_COURSE_DETAIL_SUCCESS, publicCourseService.getPublicCourseDetail(userId, publicCourseId));

    }

    @GetMapping("/user")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "getPublicCourseByUser", description = "유저가 업로드 한 코스")
    public ApiResponseDto<GetPublicCourseByUserResponseDto> getPublicCourseByUser(
            // TODO : 테스트 후
            @UserId final Long userId
            //@RequestHeader final Long userId
    ){

        return ApiResponseDto.success(SuccessStatus.GET_PUBLIC_COURSE_BY_USER_SUCCESS,
                publicCourseService.getPublicCourseByUser(userId));

    }

    @GetMapping("/total-page-count")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "getPublicCourseTotalPageCount", description = "코스발견 - 이런 코스는 어때요?의 코스 전체 개수")
    public ApiResponseDto<GetPublicCourseTotalPageCountResponseDto> getPublicCourseTotalPageCount(){
        return ApiResponseDto.success(SuccessStatus.GET_PUBLIC_COURSE_TOTAL_PAGE_COUNT_SUCCESS,
                publicCourseService.getPublicCourseTotalPageCount());
    }


    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "deletePublicCourse", description = "업로드 한 코스 삭제")
    public ApiResponseDto<DeletePublicCoursesResponseDto> deletePublicCourses(
        @UserId final Long userId,
        @Valid @RequestBody final DeletePublicCoursesRequestDto deletePublicCoursesRequestDto
    ) {
        return ApiResponseDto.success(SuccessStatus.DELETE_PUBLIC_COURSE_SUCCESS,
            publicCourseService.deletePublicCourses(userId, deletePublicCoursesRequestDto));
    }

    @PatchMapping("/{publicCourseId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "updatePublicCourse", description = "퍼블릭 코스 제목, 설명수정")
    public ApiResponseDto<UpdatePublicCourseResponseDto> updatePublicCourse(
            @UserId Long userId, @PathVariable(name = "publicCourseId") Long publicCourseId,
            @RequestBody @Valid final UpdatePublicCourseRequestDto request)  {
        return ApiResponseDto.success(SuccessStatus.UPDATE_PUBLIC_COURSE_SUCCESS, publicCourseService.updatePublicCourse(userId, publicCourseId, request.getTitle(), request.getDescription()));
    }
}
