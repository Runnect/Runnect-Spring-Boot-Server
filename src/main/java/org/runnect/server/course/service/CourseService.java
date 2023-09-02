package org.runnect.server.course.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.runnect.server.common.dto.DepartureResponse;
import org.runnect.server.common.exception.ErrorStatus;
import org.runnect.server.common.module.convert.CoordinatePathConverter;
import org.runnect.server.common.module.convert.DepartureConverter;
import org.runnect.server.course.dto.request.CourseCreateRequestDto;
import org.runnect.server.course.dto.response.CourseCreateResponseDto;
import org.runnect.server.course.dto.response.CourseGetByUserResponseDto;
import org.runnect.server.course.dto.response.CourseResponse;
import org.runnect.server.course.dto.response.CourseListDepartureResponse;
import org.runnect.server.course.dto.response.UserResponse;
import org.runnect.server.course.entity.Course;
import org.runnect.server.course.repository.CourseRepository;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.exception.userException.NotFoundUserException;
import org.runnect.server.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

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

        return CourseCreateResponseDto.of(saved.getId(), saved.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public CourseGetByUserResponseDto getCourseByUser(Long userId, Boolean includePublic) {

        List<Course> courses = new ArrayList<>();
        if (includePublic) { // 업로드 코스 포함
            courses = courseRepository.findCourseByUserId(userId);
        } else {
            courses = courseRepository.findCourseByUserIdOnlyPrivate(userId);
        }

        UserResponse userResponse = UserResponse.of(userId);

        List<CourseResponse> courseResponses = courses.stream()
            .map(course -> CourseResponse.of(
                course.getId(),
                course.getImage(),
                course.getCreatedAt(),
                new DepartureResponse(
                    course.getDepartureRegion(),
                    course.getDepartureCity(),
                    course.getDepartureTown(),
                    course.getDepartureDetail(),
                    course.getDepartureName()
                )
            )).collect(Collectors.toList());

        return CourseGetByUserResponseDto.of(userResponse, courseResponses);
    }

}