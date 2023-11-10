package org.runnect.server.publicCourse.dto.response.searchPublicCourse;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SearchPublicCourse {
    private Long id;
    private Long courseId;
    private String title;
    private String image;
    private Boolean scrap;
    private SearchPublicCourseDeparture departure;

    public static SearchPublicCourse of(Long id, Long courseId, String title, String image, Boolean scrap, String region, String city) {
        return new SearchPublicCourse(
                id, courseId, title, image, scrap,
                SearchPublicCourse.SearchPublicCourseDeparture.from(region, city));
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SearchPublicCourseDeparture {
        private String region;
        private String city;

        public static SearchPublicCourse.SearchPublicCourseDeparture from(String region, String city) {
            return new SearchPublicCourse.SearchPublicCourseDeparture(region, city);
        }
    }


}

