package org.runnect.server.publicCourse.controller;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

import javax.validation.constraints.NotBlank;

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
public class PublicCourseController {

    private final PublicCourseService publicCourseService;


    @GetMapping
    @ResponseStatus(HttpStatus.OK)
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
    public ApiResponseDto<SearchPublicCourseResponseDto> searchPublicCourse(
            @UserId final Long userId,
            @RequestParam @NotBlank String keyword
    ){
        return ApiResponseDto.success(SuccessStatus.SEARCH_PUBLIC_COURSE_SUCCESS,
                publicCourseService.searchPublicCourse(userId, keyword));
    }


    @GetMapping("/marathon")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponseDto<GetMarathonPublicCourseResponseDto> getMarathonPublicCourse(
            @UserId final Long userId
    ){
        return ApiResponseDto.success(SuccessStatus.GET_MARATHON_PUBLIC_COURSE_SUCCESS,
                publicCourseService.getMarathonPublicCourse(userId));
    }



    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
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
    public ApiResponseDto<GetPublicCourseDetailResponseDto> getPublicCourseDetail(
            @UserId final Long userId,
            @PathVariable final Long publicCourseId
    ){

        return ApiResponseDto.success(SuccessStatus.GET_PUBLIC_COURSE_DETAIL_SUCCESS, publicCourseService.getPublicCourseDetail(userId, publicCourseId));

    }

    @GetMapping("/user")
    @ResponseStatus(HttpStatus.OK)
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
    public ApiResponseDto<GetPublicCourseTotalPageCountResponseDto> getPublicCourseTotalPageCount(){
        return ApiResponseDto.success(SuccessStatus.GET_PUBLIC_COURSE_TOTAL_PAGE_COUNT_SUCCESS,
                publicCourseService.getPublicCourseTotalPageCount());
    }


    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponseDto<DeletePublicCoursesResponseDto> deletePublicCourses(
        @RequestHeader final Long userId,
        @Valid @RequestBody final DeletePublicCoursesRequestDto deletePublicCoursesRequestDto
    ) {
        return ApiResponseDto.success(SuccessStatus.DELETE_PUBLIC_COURSE_SUCCESS,
            publicCourseService.deletePublicCourses(userId, deletePublicCoursesRequestDto));
    }

    @PatchMapping("/{publicCourseId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponseDto<UpdatePublicCourseResponseDto> updatePublicCourse(
            @RequestHeader Long userId, @PathVariable(name = "publicCourseId") Long publicCourseId,
            @RequestBody @Valid final UpdatePublicCourseRequestDto request)  {
        return ApiResponseDto.success(SuccessStatus.UPDATE_PUBLIC_COURSE_SUCCESS, publicCourseService.updatePublicCourse(userId, publicCourseId, request.getTitle(), request.getDescription()));
    }
}
