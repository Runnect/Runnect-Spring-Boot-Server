package org.runnect.server.course.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CourseGetByUserResponseDto {

    private UserResponse user;
    private List<CourseResponse> courses;

    public static CourseGetByUserResponseDto of(UserResponse user, List<CourseResponse> courses) {
        return new CourseGetByUserResponseDto(user, courses);
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class UserResponse {

        private Long id;

        public static UserResponse of(Long id) {
            return new UserResponse(id);
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CourseResponse {

        private Long id;
        private String image;
        private LocalDateTime createdAt;
        private Departure departure;

        public static CourseResponse of(Long id, String image, LocalDateTime createdAt, Departure departure) {
            return new CourseResponse(id, image, createdAt, departure);
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Departure {

        private String region;
        private String city;

        public static Departure of(String region, String city) {
            return new Departure(region, city);
        }
    }

}
