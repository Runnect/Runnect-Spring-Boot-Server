package org.runnect.server.auth.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SocialInfoResponseDto {

    private String email;
    private String socialId;

    public static SocialInfoResponseDto of(final String email, final String socialId) {
        return new SocialInfoResponseDto(email, socialId);
    }
}
