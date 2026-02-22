package org.runnect.server.health.dto.request;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class HealthDataRequestDto {
    @NotNull
    private Double avgHeartRate;

    private Double maxHeartRate;

    private Double minHeartRate;

    @NotNull
    private Double calories;

    @NotNull
    private Integer zone1Seconds;

    @NotNull
    private Integer zone2Seconds;

    @NotNull
    private Integer zone3Seconds;

    @NotNull
    private Integer zone4Seconds;

    @NotNull
    private Integer zone5Seconds;

    private Double maxHeartRateConfig;

    @Valid
    private List<HeartRateSampleRequestDto> heartRateSamples;
}
