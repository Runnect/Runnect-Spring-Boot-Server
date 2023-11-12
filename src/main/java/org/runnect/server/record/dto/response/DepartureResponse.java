package org.runnect.server.record.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DepartureResponse {
    private String region;
    private String city;

    public static DepartureResponse of(String region, String city) {
        return new DepartureResponse(region, city);
    }
}
