package org.runnect.server.publicCourse.dto.response.getMarathonPublicCourse;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GetMarathonPublicCourseResponseDto {
    private List<GetMarathonPublicCourse> marathonPublicCourses;

    public static GetMarathonPublicCourseResponseDto of(List<GetMarathonPublicCourse> marathonPublicCourses) {
        return new GetMarathonPublicCourseResponseDto(marathonPublicCourses);
    }

}
