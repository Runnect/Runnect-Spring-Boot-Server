package org.runnect.server.publicCourse.dto.response.recommendPublicCourse;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RecommendPublicCourseResponseDto {
    private String ordering;
    private List<RecommendPublicCourse> publicCourses;

    public static RecommendPublicCourseResponseDto of(String ordering, List<RecommendPublicCourse> publicCourses) {
        return new RecommendPublicCourseResponseDto(ordering, publicCourses);
    }
}
