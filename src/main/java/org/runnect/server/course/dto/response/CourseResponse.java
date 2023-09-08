package org.runnect.server.course.dto.response;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.runnect.server.common.dto.DepartureResponse;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CourseResponse {
    private Long id;
    private String image;
    private LocalDateTime createdAt;
    private DepartureResponse departure;

    public static CourseResponse of(Long id, String image, LocalDateTime createdAt, DepartureResponse departure) {
        return new CourseResponse(id, image, createdAt, departure);
    }

}
