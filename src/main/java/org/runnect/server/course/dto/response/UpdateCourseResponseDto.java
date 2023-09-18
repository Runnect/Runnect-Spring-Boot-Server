package org.runnect.server.course.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.runnect.server.course.entity.Course;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateCourseResponseDto {

    private UpdateCourseResponse course;

    public static UpdateCourseResponseDto of(Course course) {
        return new UpdateCourseResponseDto(
                UpdateCourseResponse.from(course.getId(), course.getTitle()));
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class UpdateCourseResponse {
        private Long id;

        private String title;

        public static UpdateCourseResponse from(Long id, String title) {
            return new UpdateCourseResponse(id, title);
        }
    }
}
