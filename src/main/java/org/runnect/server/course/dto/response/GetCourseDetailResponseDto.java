package org.runnect.server.course.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.runnect.server.common.dto.DepartureResponse;
import org.runnect.server.common.module.convert.CoordinatePathConverter;
import org.runnect.server.course.entity.Course;
import org.runnect.server.user.entity.RunnectUser;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GetCourseDetailResponseDto {

    private UserResponseCourseDetail user;
    private CourseDetailResponse course;

    public static GetCourseDetailResponseDto of(RunnectUser user, Course course) {
        return new GetCourseDetailResponseDto(
            UserResponseCourseDetail.from(user),
            CourseDetailResponse.of(course)
        );
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class UserResponseCourseDetail {

        private Long userId;

        public static UserResponseCourseDetail from(RunnectUser user) {
            return new UserResponseCourseDetail(user.getId());
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CourseDetailResponse {

        private Long id;
        private LocalDateTime createdAt;
        private List<List<Double>> path;
        private Float distance;
        private String image;
        private DepartureResponse departure;

        public static CourseDetailResponse of(Course course) {
            return new CourseDetailResponse(
                course.getId(),
                course.getCreatedAt(),
                CoordinatePathConverter.pathConvertCoor(course.getPath()),
                course.getDistance(),
                course.getImage(),
                new DepartureResponse(course.getDepartureRegion(), course.getDepartureCity(),
                    course.getDepartureTown(), course.getDepartureDetail(),
                    course.getDepartureName())
            );
        }
    }
}
