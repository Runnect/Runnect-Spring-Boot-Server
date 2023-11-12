package org.runnect.server.publicCourse.dto.response.getMarathonPublicCourse;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.runnect.server.publicCourse.dto.response.PublicCourseDepartureResponse;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GetMarathonPublicCourse {
    private Long id;
    private Long courseId;
    private String title;
    private String image;
    private Boolean scrap;
    private PublicCourseDepartureResponse departure;

    public static GetMarathonPublicCourse of(Long id, Long courseId, String title, String image, Boolean scrap, String region, String city) {
        return new GetMarathonPublicCourse(
                id, courseId, title, image, scrap,
                PublicCourseDepartureResponse.of(region, city));
    }


}

