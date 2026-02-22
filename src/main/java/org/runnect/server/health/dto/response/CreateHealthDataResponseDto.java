package org.runnect.server.health.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateHealthDataResponseDto {
    private Long healthDataId;

    public static CreateHealthDataResponseDto of(Long healthDataId) {
        return new CreateHealthDataResponseDto(healthDataId);
    }
}
