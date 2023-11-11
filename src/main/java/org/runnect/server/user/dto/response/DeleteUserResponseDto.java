package org.runnect.server.user.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DeleteUserResponseDto {
    private Long deletedUserId;

    public static DeleteUserResponseDto of(Long deletedUserId) {
        return new DeleteUserResponseDto(deletedUserId);
    }
}
