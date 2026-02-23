package org.runnect.server.record.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RecordResponse {
    private Long id;
    private Long courseId;
    private Long publicCourseId;
    private Long userId;
    private String title;
    private String image;
    private String createdAt;
    private Float distance;
    private String time;
    private String pace;
    private DepartureResponse departure;

    public static RecordResponse of(Long id, Long courseId, Long publicCourseId, Long userId, String title,
                                    String image, String createdAt, Float distance, String time, String pace, DepartureResponse departure) {
        return new RecordResponse(id, courseId, publicCourseId, userId, title, image, createdAt, distance, time, pace, departure);
    }
}
