package org.runnect.server.auth.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SignInResponseDto implements AuthResponseDto{
    private String type;
    private String email;
    private String accessToken;
    private String refreshToken;

    public static SignInResponseDto of(final String type, final String email,
        final String accessToken, final String refreshToken) {
        return new SignInResponseDto(type, email, accessToken, refreshToken);
    }
}
