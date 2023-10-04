package org.runnect.server.auth.dto.request;

import javax.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SignInRequestDto {

    @NotBlank(message = "token은 필수 입력값입니다.")
    private String token;

    @NotBlank(message = "provider는 필수 입력값입니다.")
    private String provider;
}
