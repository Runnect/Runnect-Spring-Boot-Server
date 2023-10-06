package org.runnect.server.publicCourse.controller;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.runnect.server.common.dto.ApiResponseDto;
import org.runnect.server.common.exception.SuccessStatus;
import org.runnect.server.publicCourse.dto.request.DeletePublicCoursesRequestDto;
import org.runnect.server.publicCourse.dto.request.UpdatePublicCourseRequestDto;
import org.runnect.server.publicCourse.dto.response.DeletePublicCoursesResponseDto;
import org.runnect.server.publicCourse.dto.response.UpdatePublicCourseResponseDto;
import org.runnect.server.publicCourse.service.PublicCourseService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public-course")
public class PublicCourseController {

    private final PublicCourseService publicCourseService;

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
