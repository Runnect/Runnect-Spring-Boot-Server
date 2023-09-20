package org.runnect.server.publicCourse.controller;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.runnect.server.common.dto.ApiResponseDto;
import org.runnect.server.common.exception.SuccessStatus;
import org.runnect.server.publicCourse.dto.request.DeletePublicCoursesRequestDto;
import org.runnect.server.publicCourse.dto.response.DeletePublicCoursesResponseDto;
import org.runnect.server.publicCourse.service.PublicCourseService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
}
