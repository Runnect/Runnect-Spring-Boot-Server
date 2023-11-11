package org.runnect.server.publicCourse.dto.response.recommendPublicCourse;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.runnect.server.publicCourse.dto.response.PublicCourseDepartureResponse;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RecommendPublicCourse {
    private Long id;
    private Long courseId;
    private String title;
    private String image;
    private Boolean scrap;
    private PublicCourseDepartureResponse departure;

    public static RecommendPublicCourse of(Long id, Long courseId, String title, String image, Boolean scrap, String region, String city) {
        return new RecommendPublicCourse(
                id, courseId, title, image, scrap,
                PublicCourseDepartureResponse.of(region, city));
    }

}
