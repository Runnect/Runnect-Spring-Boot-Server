package org.runnect.server.health.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GetHealthSummaryResponseDto {
    private HealthSummaryResponse summary;

    public static GetHealthSummaryResponseDto of(HealthSummaryResponse summary) {
        return new GetHealthSummaryResponseDto(summary);
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HealthSummaryResponse {
        private Long totalRecords;
        private Long recordsWithHealth;
        private Double avgHeartRate;
        private Double avgCalories;
        private Double totalCalories;
        private GetHealthDataResponseDto.ZoneResponse zoneDistribution;

        public static HealthSummaryResponse of(Long totalRecords, Long recordsWithHealth,
                Double avgHeartRate, Double avgCalories, Double totalCalories,
                GetHealthDataResponseDto.ZoneResponse zoneDistribution) {
            return new HealthSummaryResponse(totalRecords, recordsWithHealth, avgHeartRate,
                    avgCalories, totalCalories, zoneDistribution);
        }
    }
}
