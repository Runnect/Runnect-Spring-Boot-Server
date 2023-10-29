package org.runnect.server.auth.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GetNewTokenResponseDto {
    private String accessToken;
    private String refreshToken;

    public static GetNewTokenResponseDto of( final String accessToken, final String refreshToken) {
        return new GetNewTokenResponseDto(accessToken, refreshToken);
    }
}
