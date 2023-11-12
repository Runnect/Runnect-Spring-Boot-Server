package org.runnect.server.publicCourse.dto.response.getPublicCourseByUser;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.runnect.server.publicCourse.dto.response.PublicCourseDepartureResponse;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GetPublicCourseByUserPublicCourse {
    private Long id;
    private Long courseId;
    private Boolean scrap;
    private String title;
    private String image;
    private PublicCourseDepartureResponse departure;

    public static GetPublicCourseByUserPublicCourse of(Long id, Long courseId, Boolean scrap, String title, String image, String region, String city){
        return new GetPublicCourseByUserPublicCourse(id, courseId, scrap, title, image,
                PublicCourseDepartureResponse.of(region, city));

    }

}

