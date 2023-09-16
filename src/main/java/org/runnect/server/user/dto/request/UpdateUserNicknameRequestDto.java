package org.runnect.server.user.dto.request;

import javax.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateUserNicknameRequestDto {

    @NotBlank(message = "닉네임은 필수 입력값입니다.")
    private String nickname;
}
