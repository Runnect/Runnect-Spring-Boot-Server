package org.runnect.server.user.controller;

import lombok.RequiredArgsConstructor;
import org.runnect.server.common.dto.ApiResponseDto;
import org.runnect.server.common.exception.SuccessStatus;
import org.runnect.server.user.dto.response.MyPageResponseDto;
import org.runnect.server.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponseDto<MyPageResponseDto> getMyPage(@RequestHeader Long userId) {
        return ApiResponseDto.success(
            SuccessStatus.GET_MY_PAGE_SUCCESS, userService.getMyPage(userId));
    }
}
