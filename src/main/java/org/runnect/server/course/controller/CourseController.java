package org.runnect.server.course.controller;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.runnect.server.common.constant.SuccessStatus;
import org.runnect.server.common.dto.ApiResponseDto;
import org.runnect.server.common.resolver.userId.UserId;
import org.runnect.server.course.dto.request.CourseCreateRequestDto;
import org.runnect.server.course.dto.request.DeleteCoursesRequestDto;
import org.runnect.server.course.dto.request.UpdateCourseRequestDto;
import org.runnect.server.course.dto.response.CourseCreateResponseDto;
import org.runnect.server.course.dto.response.CourseGetByUserResponseDto;
import org.runnect.server.course.dto.response.DeleteCoursesResponseDto;
import org.runnect.server.course.dto.response.GetCourseDetailResponseDto;
import org.runnect.server.course.dto.response.UpdateCourseResponseDto;
import org.runnect.server.course.service.CourseService;
import org.runnect.server.external.aws.S3Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/course")
public class CourseController {

    private final S3Service s3Service;
    private final CourseService courseService;

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponseDto<CourseCreateResponseDto> createCourse(
        @UserId Long userId,
        @RequestPart @Valid final CourseCreateRequestDto data,
        @RequestPart final MultipartFile image
    ) {
        String imageUrl = s3Service.uploadImage(image, "course");
        return ApiResponseDto.success(SuccessStatus.CREATE_COURSE_SUCCESS,
            courseService.createCourse(userId, data, imageUrl));
    }

    @GetMapping("/user")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponseDto<CourseGetByUserResponseDto> getCourseByUser(@UserId Long userId) {
        return ApiResponseDto.success(SuccessStatus.GET_COURSE_LIST_BY_USER_SUCCESS,
            courseService.getCourseByUser(userId));
    }

    @GetMapping("/private/user")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponseDto<CourseGetByUserResponseDto> getPrivateCourseByUser(
        @UserId Long userId) {
        return ApiResponseDto.success(SuccessStatus.GET_COURSE_LIST_BY_USER_SUCCESS,
            courseService.getPrivateCourseByUser(userId));
    }

    @GetMapping("/detail/{courseId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponseDto<GetCourseDetailResponseDto> getCourseDetail(@UserId Long userId,
        @PathVariable Long courseId) {
        return ApiResponseDto.success(SuccessStatus.GET_COURSE_DETAIL_SUCCESS,
            courseService.getCourseDetail(courseId));
    }

    @PatchMapping("/{courseId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponseDto<UpdateCourseResponseDto> updateCourse(@UserId Long userId,
        @PathVariable(name = "courseId") Long courseId,
        @RequestBody @Valid final UpdateCourseRequestDto request) {
        return ApiResponseDto.success(SuccessStatus.UPDATE_COURSE_SUCCESS,
            courseService.updateCourse(userId, courseId, request.getTitle()));
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponseDto<DeleteCoursesResponseDto> deleteCourses(
        @UserId Long userId,
        @Valid @RequestBody final DeleteCoursesRequestDto deleteCoursesRequestDto
    ) {
        return ApiResponseDto.success(SuccessStatus.DELETE_COURSES_SUCCESS,
            courseService.deleteCourses(deleteCoursesRequestDto.getCourseIdList(), userId));
    }
}
