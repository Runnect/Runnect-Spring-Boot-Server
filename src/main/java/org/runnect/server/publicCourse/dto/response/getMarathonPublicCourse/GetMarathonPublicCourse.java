package org.runnect.server.publicCourse.dto.response.getMarathonPublicCourse;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GetMarathonPublicCourse {
    private Long id;
    private Long courseId;
    private String title;
    private String image;
    private Boolean scrap;
    private GetMarathonPublicCourseDeparture departure;

    public static GetMarathonPublicCourse of(Long id, Long courseId, String title, String image, Boolean scrap, String region, String city) {
        return new GetMarathonPublicCourse(
                id, courseId, title, image, scrap,
                GetMarathonPublicCourse.GetMarathonPublicCourseDeparture.from(region, city));
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetMarathonPublicCourseDeparture {
        private String region;
        private String city;

        public static GetMarathonPublicCourse.GetMarathonPublicCourseDeparture from(String region, String city) {
            return new GetMarathonPublicCourse.GetMarathonPublicCourseDeparture(region, city);
        }
    }


}

