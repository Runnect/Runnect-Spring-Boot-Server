package org.runnect.server.health.dto.response;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GetHealthDataResponseDto {
    private HealthDataDetailResponse healthData;

    public static GetHealthDataResponseDto of(HealthDataDetailResponse healthData) {
        return new GetHealthDataResponseDto(healthData);
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HealthDataDetailResponse {
        private Long id;
        private Long recordId;
        private Double avgHeartRate;
        private Double maxHeartRate;
        private Double minHeartRate;
        private Double calories;
        private ZoneResponse zones;
        private Double maxHeartRateConfig;
        private List<HeartRateSampleResponse> heartRateSamples;

        public static HealthDataDetailResponse of(Long id, Long recordId, Double avgHeartRate,
                Double maxHeartRate, Double minHeartRate, Double calories,
                ZoneResponse zones, Double maxHeartRateConfig,
                List<HeartRateSampleResponse> heartRateSamples) {
            return new HealthDataDetailResponse(id, recordId, avgHeartRate, maxHeartRate,
                    minHeartRate, calories, zones, maxHeartRateConfig, heartRateSamples);
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ZoneResponse {
        private Integer zone1Seconds;
        private Integer zone2Seconds;
        private Integer zone3Seconds;
        private Integer zone4Seconds;
        private Integer zone5Seconds;

        public static ZoneResponse of(Integer zone1Seconds, Integer zone2Seconds,
                Integer zone3Seconds, Integer zone4Seconds, Integer zone5Seconds) {
            return new ZoneResponse(zone1Seconds, zone2Seconds, zone3Seconds, zone4Seconds, zone5Seconds);
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HeartRateSampleResponse {
        private Double heartRate;
        private Integer elapsedSeconds;
        private Integer zone;

        public static HeartRateSampleResponse of(Double heartRate, Integer elapsedSeconds, Integer zone) {
            return new HeartRateSampleResponse(heartRate, elapsedSeconds, zone);
        }
    }
}
