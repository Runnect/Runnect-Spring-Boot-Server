package org.runnect.server.user.dto.response;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.entity.StampType;
import org.runnect.server.user.entity.UserStamp;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FindUserStampsResponseDto {

    private UserInfo user;
    private List<StampInfo> stamps;

    public static FindUserStampsResponseDto from(RunnectUser user) {
        return new FindUserStampsResponseDto(
            UserInfo.from(user),
            user.getUserStamps().stream()
                .map(StampInfo::from)
                .collect(Collectors.toList()));
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static class UserInfo {

        private Long id;

        private static UserInfo from(RunnectUser user) {
            return new UserInfo(user.getId());
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static class StampInfo {

        private StampType id;

        private static StampInfo from(UserStamp stamp) {
            return new StampInfo(stamp.getStampId());
        }
    }
}
