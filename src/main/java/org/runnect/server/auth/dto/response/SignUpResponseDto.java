package org.runnect.server.auth.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SignUpResponseDto implements AuthResponseDto{

    private String type;
    private String email;
    private String nickname;
    private String accessToken;
    private String refreshToken;

    public static SignUpResponseDto of(final String type, final String email, final String nickname,
                                       final String accessToken, final String refreshToken) {
        return new SignUpResponseDto(type, email, nickname, accessToken, refreshToken);
    }
}