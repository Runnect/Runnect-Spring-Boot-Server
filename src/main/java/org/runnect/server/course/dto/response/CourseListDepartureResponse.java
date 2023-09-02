package org.runnect.server.course.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CourseListDepartureResponse {

    private String region;
    private String city;

    public static CourseListDepartureResponse of(String region, String city) {
        return new CourseListDepartureResponse(region, city);
    }

}
