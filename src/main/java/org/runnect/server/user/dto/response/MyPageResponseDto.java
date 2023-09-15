package org.runnect.server.user.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.runnect.server.user.entity.RunnectUser;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MyPageResponseDto {

    private MyPageResponse user;

    public static MyPageResponseDto of(final RunnectUser user, int levelPercent) {
        return new MyPageResponseDto(MyPageResponse.of(user, levelPercent));
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class MyPageResponse {

        private Long id;
        private String email;
        private String provider;
        private String nickname;
        private String latestStamp;
        private int level;
        private int levelPercent;

        public static MyPageResponse of(RunnectUser user, int levelPercent) {
            return new MyPageResponse(user.getId(), user.getEmail(), user.getProvider().toString(),
                user.getNickname(), user.getLatestStamp().toString(), user.getLevel(),
                levelPercent);
        }
    }
}
