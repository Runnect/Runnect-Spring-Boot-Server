package org.runnect.server.course.controller;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.runnect.server.common.dto.ApiResponseDto;
import org.runnect.server.common.exception.BadRequestException;
import org.runnect.server.common.exception.ErrorStatus;
import org.runnect.server.common.exception.SuccessStatus;
import org.runnect.server.course.dto.request.CourseCreateRequestDto;
import org.runnect.server.course.dto.request.UpdateCourseRequestDto;
import org.runnect.server.course.dto.response.CourseCreateResponseDto;
import org.runnect.server.course.dto.response.CourseGetByUserResponseDto;
import org.runnect.server.course.dto.response.GetCourseDetailResponseDto;
import org.runnect.server.course.dto.response.UpdateCourseResponseDto;
import org.runnect.server.course.service.CourseService;
import org.runnect.server.external.aws.S3Service;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
        @ModelAttribute @Valid final CourseCreateRequestDto courseCreateRequestDto,
        BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(ErrorStatus.REQUEST_VALIDATION_EXCEPTION,
                bindingResult.getFieldError().getField() + " 필드가 입력되지 않았습니다.");
        }
        String imageUrl = s3Service.uploadImage(courseCreateRequestDto.getImage(), "course");
        return ApiResponseDto.success(SuccessStatus.CREATE_COURSE_SUCCESS,
            courseService.createCourse(userId, courseCreateRequestDto, imageUrl));
    }

    @GetMapping("/user")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponseDto<CourseGetByUserResponseDto> getCourseByUser(@RequestHeader Long userId) {
        return ApiResponseDto.success(SuccessStatus.GET_COURSE_LIST_BY_USER, courseService.getCourseByUser(userId));
    }

    @GetMapping("/private/user")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponseDto<CourseGetByUserResponseDto> getPrivateCourseByUser(@RequestHeader Long userId) {
        return ApiResponseDto.success(SuccessStatus.GET_COURSE_LIST_BY_USER, courseService.getPrivateCourseByUser(userId));
    }

    @GetMapping("/detail/{courseId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponseDto<GetCourseDetailResponseDto> getCourseDetail(@RequestHeader Long userId, @PathVariable Long courseId) {
        return ApiResponseDto.success(SuccessStatus.GET_COURSE_DETAIL_SUCCESS, courseService.getCourseDetail(courseId));
    }

    @PatchMapping("/{courseId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponseDto<UpdateCourseResponseDto> updateCourse(@RequestHeader Long userId, @PathVariable(name = "courseId") Long courseId,
                                                                @RequestBody @Valid final UpdateCourseRequestDto request) {
        return ApiResponseDto.success(SuccessStatus.UPDATE_COURSE_SUCCESS, courseService.updateCourse(userId, courseId, request.getTitle()));
    }


}
