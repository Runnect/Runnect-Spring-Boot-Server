package org.runnect.server.scrap.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ScrapResponse {
    private Long id;
    private Long publicCourseId;
    private Long courseId;
    private String title;
    private String image;
    private DepartureResponse departure;

    public static ScrapResponse of (Long id, Long publicCourseId, Long courseId, String title, String image, DepartureResponse departure) {
        return new ScrapResponse(id, publicCourseId, courseId, title, image, departure);
    }
}
