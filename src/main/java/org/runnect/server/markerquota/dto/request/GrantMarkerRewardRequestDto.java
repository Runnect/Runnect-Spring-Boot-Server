package org.runnect.server.markerquota.dto.request;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class GrantMarkerRewardRequestDto {
    @NotNull
    @Min(1)
    private Integer amount;

    /**
     * 광고 SDK가 발급하는, 시청 1건당 고유한 식별자. 같은 값이 중복으로 들어와도
     * MarkerRewardClaim의 유니크 제약으로 한 번만 지급된다.
     */
    @NotBlank
    private String rewardTransactionId;
}
