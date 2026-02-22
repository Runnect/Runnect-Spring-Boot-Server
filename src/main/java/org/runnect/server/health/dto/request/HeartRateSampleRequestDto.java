package org.runnect.server.health.dto.request;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class HeartRateSampleRequestDto {
    @NotNull
    private Double heartRate;

    @NotNull
    private Integer elapsedSeconds;

    @NotNull
    @Min(1) @Max(5)
    private Integer zone;
}
