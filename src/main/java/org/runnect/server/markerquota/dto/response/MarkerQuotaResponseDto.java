package org.runnect.server.markerquota.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MarkerQuotaResponseDto {
    private int balance;

    public static MarkerQuotaResponseDto of(int balance) {
        return new MarkerQuotaResponseDto(balance);
    }
}
