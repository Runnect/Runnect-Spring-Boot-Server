package org.runnect.server.publicCourse.dto.response.searchPublicCourse;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SearchPublicCourseResponseDto {
    private List<SearchPublicCourse> publicCourses;

    public static SearchPublicCourseResponseDto of(List<SearchPublicCourse> publicCourses) {
        return new SearchPublicCourseResponseDto(publicCourses);
    }
}
