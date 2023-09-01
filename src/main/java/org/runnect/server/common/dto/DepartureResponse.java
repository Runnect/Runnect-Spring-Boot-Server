package org.runnect.server.common.dto;

import lombok.Getter;

@Getter
public class DepartureResponse {
    private String region;
    private String city;
    private String town;
    private String detail;
    private String name;

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
}
