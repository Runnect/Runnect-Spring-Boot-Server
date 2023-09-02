package org.runnect.server.course.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.postgresql.geometric.PGpath;
import org.postgresql.util.PGobject;
import org.runnect.server.common.dto.DepartureResponse;
import org.runnect.server.common.exception.ErrorStatus;
import org.runnect.server.common.exception.SuccessStatus;
import org.runnect.server.common.module.convert.CoordinatePathConverter;
import org.runnect.server.common.module.convert.DepartureConverter;
import org.runnect.server.course.dto.request.CourseCreateRequestDto;
import org.runnect.server.course.dto.response.CourseCreateResponseDto;
import org.runnect.server.course.dto.response.CourseGetByUserResponseDto;
import org.runnect.server.course.dto.response.CourseGetByUserResponseDto.CourseResponse;
import org.runnect.server.course.dto.response.CourseGetByUserResponseDto.Departure;
import org.runnect.server.course.dto.response.CourseGetByUserResponseDto.UserResponse;
import org.runnect.server.course.entity.Course;
import org.runnect.server.course.repository.CourseRepository;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.exception.userException.NotFoundUserException;
import org.runnect.server.user.repository.UserRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Transactional
    public CourseCreateResponseDto createCourse(Long userId, CourseCreateRequestDto requestDto, String image) {
        RunnectUser user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundUserException(ErrorStatus.NOT_FOUND_USER_EXCEPTION, ErrorStatus.NOT_FOUND_USER_EXCEPTION.getMessage()));

        String path = CoordinatePathConverter.coorConvertPath(requestDto.getPath());
        DepartureResponse departureResponse = DepartureConverter.requestConvertDeparture(requestDto.getDepartureAddress(), requestDto.getDepartureName());

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
//        RunnectUser user = userRepository.findById(userId)
//            .orElseThrow(() -> new NotFoundUserException(ErrorStatus.NOT_FOUND_USER_EXCEPTION, ErrorStatus.NOT_FOUND_USER_EXCEPTION.getMessage()));

        List<Course> courses = new ArrayList<>();
        if (includePublic) { // 업로드 코스 포함
            courses = courseRepository.findByRunnectUserOrderByCreatedAtDesc(userId);
        } else {
            courses = courseRepository.findByRunnectUserAndIsPrivateTrueOrderByCreatedAtDesc(userId);
        }

        UserResponse userResponse = UserResponse.of(userId);

        List<CourseResponse> courseResponses = courses.stream()
                .map(course -> CourseResponse.of(
                    course.getId(),
                    course.getImage(),
                    course.getCreatedAt(),
                    Departure.of(
                        course.getDepartureRegion(),
                        course.getDepartureCity()
                    )
                )).collect(Collectors.toList());

        return CourseGetByUserResponseDto.of(userResponse, courseResponses);
    }

}
