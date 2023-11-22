package org.runnect.server.auth.controller;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import lombok.RequiredArgsConstructor;
import org.runnect.server.auth.dto.request.SignInRequestDto;
import org.runnect.server.auth.dto.response.AuthResponseDto;
import org.runnect.server.auth.dto.response.GetNewTokenResponseDto;
import org.runnect.server.auth.dto.response.SignInResponseDto;
import org.runnect.server.auth.service.AuthService;
import org.runnect.server.common.dto.ApiResponseDto;
import org.runnect.server.common.constant.SuccessStatus;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private final AuthService authService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponseDto<AuthResponseDto> signIn(@Valid @RequestBody SignInRequestDto requestDto) {
        AuthResponseDto result = authService.signIn(requestDto);
        if(result.getClass().equals(SignInResponseDto.class)){
            return ApiResponseDto.success(SuccessStatus.LOGIN_SUCCESS, result);
        }
        else{
            return ApiResponseDto.success(SuccessStatus.SIGNUP_SUCCESS, result);
        }
    }



    @GetMapping("/getNewToken")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponseDto<GetNewTokenResponseDto> getNewToken(@RequestHeader @NotBlank String accessToken,
                                                              @RequestHeader @NotBlank String refreshToken){

        return ApiResponseDto.success(SuccessStatus.NEW_TOKEN_SUCCESS, authService.getNewToken(accessToken, refreshToken));

    }
}
