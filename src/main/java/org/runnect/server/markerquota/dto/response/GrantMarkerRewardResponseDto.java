package org.runnect.server.markerquota.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GrantMarkerRewardResponseDto {
    private boolean granted;
    private int balance;

    public static GrantMarkerRewardResponseDto of(boolean granted, int balance) {
        return new GrantMarkerRewardResponseDto(granted, balance);
    }
}
