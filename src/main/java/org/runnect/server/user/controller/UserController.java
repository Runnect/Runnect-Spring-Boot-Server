package org.runnect.server.user.controller;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.runnect.server.common.dto.ApiResponseDto;
import org.runnect.server.common.constant.SuccessStatus;
import org.runnect.server.user.dto.request.UpdateUserNicknameRequestDto;
import org.runnect.server.user.dto.response.MyPageResponseDto;
import org.runnect.server.user.dto.response.UserProfileResponseDto;
import org.runnect.server.user.dto.response.UpdateUserNicknameResponseDto;
import org.runnect.server.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponseDto<UpdateUserNicknameResponseDto> updateUserNickname(
        @RequestHeader Long userId,
        @Valid @RequestBody UpdateUserNicknameRequestDto updateUserNicknameRequestDto
    ) {
        return ApiResponseDto.success(SuccessStatus.UPDATE_USER_NICKNAME_SUCCESS,
            userService.updateUserNickname(userId, updateUserNicknameRequestDto));
    }

    @GetMapping("/{profileUserId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponseDto<UserProfileResponseDto> getUserProfile(
        @RequestHeader Long userId,
        @PathVariable Long profileUserId
    ) {
        return ApiResponseDto.success(SuccessStatus.GET_USER_PROFILE_SUCCESS,
            userService.getUserProfile(profileUserId, userId));
    }
}
