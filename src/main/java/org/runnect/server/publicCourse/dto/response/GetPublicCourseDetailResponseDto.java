package org.runnect.server.publicCourse.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GetPublicCourseDetailResponseDto {
    private GetPublicCourseDetailUser user;
    private GetPublicCourseDetailPublicCourse publicCourse;

    public static GetPublicCourseDetailResponseDto of(String nickname, int level, String userImage, Boolean isNowUser,
                                                      Long publicCourseId, Long courseId, Boolean isScrap, Long scrapCount,
                                                      String courseImage, String title, String description, List<List<Double>> path,
                                                      Float distance, String region, String city, String town, String name) {
        return new GetPublicCourseDetailResponseDto(
                GetPublicCourseDetailResponseDto.GetPublicCourseDetailUser.from(nickname, level, userImage, isNowUser),
                GetPublicCourseDetailResponseDto.GetPublicCourseDetailPublicCourse.from(publicCourseId, courseId, isScrap, scrapCount, courseImage, title, description, path, distance, region, city, town, name));
    }


    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetPublicCourseDetailUser {
        private String nickname;
        private int level;
        private String image;
        private Boolean isNowUser;

        public static GetPublicCourseDetailResponseDto.GetPublicCourseDetailUser from(String nickname, int level,
                                                                                      String image, Boolean isNowUser) {
            return new GetPublicCourseDetailResponseDto.GetPublicCourseDetailUser(nickname, level, image, isNowUser);
        }
    }


    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetPublicCourseDetailPublicCourse {
        private Long id;
        private Long courseId;
        private Boolean isScrap;
        private Long scrapCount;
        private String image;
        private String title;
        private String description;
        private List<List<Double>> path;
        private Float distance;
        private GetPublicCourseDetailPublicCourseDeparture departure;

        public static GetPublicCourseDetailResponseDto.GetPublicCourseDetailPublicCourse from(Long publicCourseId, Long courseId,
                                                                                              Boolean isScrap, Long scrapCount,
                                                                                              String courseImage, String title,
                                                                                              String description, List<List<Double>> path,
                                                                                              Float distance, String region,
                                                                                              String city, String town, String name) {
            return new GetPublicCourseDetailResponseDto
                    .GetPublicCourseDetailPublicCourse(publicCourseId, courseId, isScrap, scrapCount,
                    courseImage, title, description, path, distance,
                    GetPublicCourseDetailResponseDto.GetPublicCourseDetailPublicCourse
                    .GetPublicCourseDetailPublicCourseDeparture.from(region, city, town, name));
        }


        @Getter
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        @AllArgsConstructor(access = AccessLevel.PRIVATE)
        public static class GetPublicCourseDetailPublicCourseDeparture {
            private String region;
            private String city;
            private String town;
            private String name;

            public static GetPublicCourseDetailResponseDto
                    .GetPublicCourseDetailPublicCourse
                    .GetPublicCourseDetailPublicCourseDeparture from(String region, String city,
                                                                     String town, String name) {
                return new GetPublicCourseDetailResponseDto
                        .GetPublicCourseDetailPublicCourse
                        .GetPublicCourseDetailPublicCourseDeparture(region, city, town, name);
            }
        }
    }
}


