package org.runnect.server.record.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HealthDataResponse {
    private Double avgHeartRate;
    private Double calories;

    public static HealthDataResponse of(Double avgHeartRate, Double calories) {
        return new HealthDataResponse(avgHeartRate, calories);
    }
}
