package org.runnect.server.course.dto.response;

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
}
