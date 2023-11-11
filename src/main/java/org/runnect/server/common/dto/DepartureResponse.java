package org.runnect.server.common.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.runnect.server.course.entity.Course;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DepartureResponse {
    private String region;
    private String city;
    private String town;
    private String detail;
    private String name;

    public DepartureResponse(String region, String city, String town) {
        this.region = region;
        this.city = city;
        this.town = town;
    }
    public DepartureResponse(String region, String city, String town, String name) {
        this.region = region;
        this.city = city;
        this.town = town;
        this.name = name;
    }

    public DepartureResponse(String region, String city, String town, String detail, String name) {
        this.region = region;
        this.city = city;
        this.town = town;
        this.detail = detail;
        this.name = name;
    }

    public static DepartureResponse from(Course course) {
        return new DepartureResponse(
            course.getDepartureRegion(),
            course.getDepartureCity(),
            course.getDepartureTown(),
            course.getDepartureDetail(),
            course.getDepartureName()
        );
    }
}
