package org.runnect.server.auth.controller;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.runnect.server.auth.dto.request.SignInRequestDto;
import org.runnect.server.auth.dto.response.SignInResponseDto;
import org.runnect.server.auth.service.AuthService;
import org.runnect.server.common.dto.ApiResponseDto;
import org.runnect.server.common.exception.SuccessStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponseDto<SignInResponseDto> signIn(@Valid @RequestBody SignInRequestDto requestDto) {
        return ApiResponseDto.success(SuccessStatus.LOGIN_SUCCESS, authService.signIn(requestDto));
    }
}
