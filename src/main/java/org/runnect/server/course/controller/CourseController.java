package org.runnect.server.course.controller;

import lombok.RequiredArgsConstructor;
import org.runnect.server.common.dto.ApiResponseDto;
import org.runnect.server.common.exception.SuccessStatus;
import org.runnect.server.common.resolver.userId.UserId;
import org.runnect.server.course.dto.request.CourseCreateRequestDto;
import org.runnect.server.course.dto.response.CourseCreateResponseDto;
import org.runnect.server.course.service.CourseService;
import org.runnect.server.external.aws.S3Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/course")
public class CourseController {

    private final S3Service s3Service;
    private final CourseService courseService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponseDto<CourseCreateResponseDto> createCourse(
            @RequestHeader Long userId,
            @ModelAttribute @Valid final CourseCreateRequestDto courseCreateRequestDto
    ) {
        String imageUrl = s3Service.uploadImage(courseCreateRequestDto.getImage(), "course");
        return ApiResponseDto.success(SuccessStatus.CREATE_COURSE_SUCCESS, courseService.createCourse(userId, courseCreateRequestDto, imageUrl));
    }
}
