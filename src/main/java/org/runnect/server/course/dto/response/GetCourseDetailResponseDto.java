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

    public static GetCourseDetailResponseDto of(Course course, Boolean isNowUser) {
        return new GetCourseDetailResponseDto(
            UserResponseCourseDetail.from(course.getRunnectUser()),
            CourseDetailResponse.of(course, isNowUser)
        );
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class UserResponseCourseDetail {

        private Long userId;

        public static UserResponseCourseDetail from(RunnectUser user) {
            return new UserResponseCourseDetail(user != null ? user.getId() : -1);
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
        private String title;
        private DepartureResponse departure;
        private Boolean isNowUser;

        public static CourseDetailResponse of(Course course, Boolean isNowUser) {
            return new CourseDetailResponse(
                course.getId(),
                course.getCreatedAt(),
                CoordinatePathConverter.pathConvertCoor(course.getPath()),
                course.getDistance(),
                course.getImage(),
                course.getTitle(),
                DepartureResponse.from(course),
                isNowUser
            );
        }
    }
}
