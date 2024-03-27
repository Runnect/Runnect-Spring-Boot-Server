package org.runnect.server.auth.controller;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Auth", description = "Auth API Document")
public class AuthController {

    private final AuthService authService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Social Login & Sign Up", description = "소셜 로그인 및 회원가입")
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
    @Operation(summary = "getNewToken", description = "accessToken 재발급")
    public ApiResponseDto<GetNewTokenResponseDto> getNewToken(@RequestHeader @NotBlank String accessToken,
                                                              @RequestHeader @NotBlank String refreshToken){

        return ApiResponseDto.success(SuccessStatus.NEW_TOKEN_SUCCESS, authService.getNewToken(accessToken, refreshToken));

    }
}
