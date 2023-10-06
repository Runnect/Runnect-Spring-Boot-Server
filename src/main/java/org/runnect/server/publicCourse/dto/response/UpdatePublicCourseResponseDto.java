package org.runnect.server.publicCourse.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.runnect.server.publicCourse.entity.PublicCourse;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdatePublicCourseResponseDto {

    private UpdatePublicCourseResponse publicCourse;

    public static UpdatePublicCourseResponseDto of(PublicCourse publicCourse) {
        return new UpdatePublicCourseResponseDto(
                UpdatePublicCourseResponse.from(publicCourse.getId(), publicCourse.getTitle(), publicCourse.getDescription()));
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class UpdatePublicCourseResponse {
        private Long id;
        private String title;
        private String description;

        public static UpdatePublicCourseResponse from(Long id, String title, String description) {
            return new UpdatePublicCourseResponse(id, title, description);
        }
    }
}
