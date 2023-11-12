package org.runnect.server.publicCourse.dto.response.getPublicCourseByUser;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GetPublicCourseByUserPublicCourse {
    private Long id;
    private Long courseId;
    private Boolean scrap;
    private String title;
    private String image;
    private GetPublicCourseByUserPublicCourseDeparture departure;

    public static GetPublicCourseByUserPublicCourse of(Long id, Long courseId, Boolean scrap, String title, String image, String region, String city){
        return new GetPublicCourseByUserPublicCourse(id, courseId, scrap, title, image,
                GetPublicCourseByUserPublicCourseDeparture.from(region,city));

    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetPublicCourseByUserPublicCourseDeparture{
        private String region;
        private String city;

        public static GetPublicCourseByUserPublicCourseDeparture from(String region, String city) {
            return new GetPublicCourseByUserPublicCourseDeparture(region, city);
        }
    }



}

