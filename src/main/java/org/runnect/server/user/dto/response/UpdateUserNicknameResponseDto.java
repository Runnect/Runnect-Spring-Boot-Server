package org.runnect.server.user.dto.response;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.runnect.server.user.entity.RunnectUser;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateUserNicknameResponseDto {

    private UpdateUserNicknameResponse user;

    public static UpdateUserNicknameResponseDto of(final RunnectUser user, final int levelPercent) {
        return new UpdateUserNicknameResponseDto(UpdateUserNicknameResponse.of(user, levelPercent));
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class UpdateUserNicknameResponse {

        private Long id;
        private String nickname;
        private String latestStamp;
        private int level;
        private int levelPercent;
        private LocalDateTime modifiedAt;

        public static UpdateUserNicknameResponse of(RunnectUser user, int levelPercent) {
            return new UpdateUserNicknameResponse(user.getId(), user.getNickname(),
                user.getLatestStamp().toString(), user.getLevel(), levelPercent,
                user.getUpdatedAt());
        }
    }
}
