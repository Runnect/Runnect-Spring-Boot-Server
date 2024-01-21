package org.runnect.server.publicCourse.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PublicCourseDepartureResponse {
    private String region;
    private String city;

    public static PublicCourseDepartureResponse of(String region, String city) {
        return new PublicCourseDepartureResponse(region, city);
    }
}
