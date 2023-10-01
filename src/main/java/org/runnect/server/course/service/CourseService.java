package org.runnect.server.course.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.runnect.server.common.dto.DepartureResponse;
import org.runnect.server.common.exception.ErrorStatus;
import org.runnect.server.common.exception.NotFoundException;
import org.runnect.server.common.module.convert.CoordinatePathConverter;
import org.runnect.server.common.module.convert.DepartureConverter;
import org.runnect.server.course.dto.request.CourseCreateRequestDto;
import org.runnect.server.course.dto.response.*;
import org.runnect.server.course.entity.Course;
import org.runnect.server.course.repository.CourseRepository;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.entity.StampType;
import org.runnect.server.user.exception.userException.NotFoundUserException;
import org.runnect.server.user.repository.UserRepository;
import org.runnect.server.user.service.UserStampService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final UserStampService userStampService;

    @Transactional
    public CourseCreateResponseDto createCourse(Long userId, CourseCreateRequestDto requestDto,
        String image) {
        RunnectUser user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundUserException(ErrorStatus.NOT_FOUND_USER_EXCEPTION,
                ErrorStatus.NOT_FOUND_USER_EXCEPTION.getMessage()));

        String path = CoordinatePathConverter.coorConvertPath(requestDto.getPath());
        DepartureResponse departureResponse = DepartureConverter.requestConvertDeparture(
            requestDto.getDepartureAddress(), requestDto.getDepartureName());

        Course course = Course.builder()
            .runnectUser(user)
            .title(requestDto.getTitle())
            .departureRegion(departureResponse.getRegion())
            .departureCity(departureResponse.getCity())
            .departureTown(departureResponse.getTown())
            .departureDetail(departureResponse.getDetail())
            .departureName(departureResponse.getName())
            .distance(requestDto.getDistance())
            .image(image)
            .path(path)
            .build();

        Course saved = courseRepository.save(course);
        user.updateCreatedCourse();
        userStampService.createStampByUser(user, StampType.C);

        return CourseCreateResponseDto.of(saved.getId(), saved.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public CourseGetByUserResponseDto getCourseByUser(Long userId) {
        RunnectUser user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundUserException(ErrorStatus.NOT_FOUND_USER_EXCEPTION,
                ErrorStatus.NOT_FOUND_USER_EXCEPTION.getMessage()));

        List<Course> courses = courseRepository.findCourseByUserId(userId);

        List<CourseResponse> courseResponses = courses.stream()
            .map(course -> CourseResponse.of(
                course.getId(),
                course.getImage(),
                course.getCreatedAt(),
                course.getTitle(),
                DepartureResponse.from(course)
            )).collect(Collectors.toList());

        return CourseGetByUserResponseDto.of(UserResponse.of(user.getId()), courseResponses);
    }

    @Transactional(readOnly = true)
    public CourseGetByUserResponseDto getPrivateCourseByUser(Long userId) {
        RunnectUser user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundUserException(ErrorStatus.NOT_FOUND_USER_EXCEPTION,
                ErrorStatus.NOT_FOUND_USER_EXCEPTION.getMessage()));

        List<Course> courses = courseRepository.findCourseByUserIdOnlyPrivate(userId);

        List<CourseResponse> courseResponses = courses.stream()
            .map(course -> CourseResponse.of(
                course.getId(),
                course.getImage(),
                course.getCreatedAt(),
                course.getTitle(),
                DepartureResponse.from(course)
            )).collect(Collectors.toList());

        return CourseGetByUserResponseDto.of(UserResponse.of(user.getId()), courseResponses);
    }

    @Transactional(readOnly = true)
    public GetCourseDetailResponseDto getCourseDetail(Long courseId) {

        Course course = courseRepository.findCourseByIdFetchUser(courseId)
            .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_COURSE_EXCEPTION,
                ErrorStatus.NOT_FOUND_COURSE_EXCEPTION.getMessage()));

        return GetCourseDetailResponseDto.of(course.getRunnectUser(), course);
    }

    @Transactional
    public UpdateCourseResponseDto updateCourse(Long userId, Long courseId, String title) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(()->new NotFoundException(ErrorStatus.NOT_FOUND_COURSE_EXCEPTION, ErrorStatus.NOT_FOUND_COURSE_EXCEPTION.getMessage()));

        course.updateCourse(title);

        return UpdateCourseResponseDto.of(course);
    }

}
