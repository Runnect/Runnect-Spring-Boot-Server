package org.runnect.server.user.controller;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import lombok.RequiredArgsConstructor;
import org.runnect.server.common.dto.ApiResponseDto;
import org.runnect.server.common.constant.SuccessStatus;
import org.runnect.server.common.resolver.userId.UserId;
import org.runnect.server.user.dto.request.UpdateUserNicknameRequestDto;
import org.runnect.server.user.dto.response.DeleteUserResponseDto;
import org.runnect.server.user.dto.response.MyPageResponseDto;
import org.runnect.server.user.dto.response.UserProfileResponseDto;
import org.runnect.server.user.dto.response.UpdateUserNicknameResponseDto;
import org.runnect.server.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;


    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponseDto<MyPageResponseDto> getMyPage(@UserId Long userId) {
        return ApiResponseDto.success(
            SuccessStatus.GET_MY_PAGE_SUCCESS, userService.getMyPage(userId));
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponseDto<UpdateUserNicknameResponseDto> updateUserNickname(
        @UserId Long userId,
        @Valid @RequestBody UpdateUserNicknameRequestDto updateUserNicknameRequestDto
    ) {
        return ApiResponseDto.success(SuccessStatus.UPDATE_USER_NICKNAME_SUCCESS,
            userService.updateUserNickname(userId, updateUserNicknameRequestDto));
    }

    @GetMapping("/{profileUserId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponseDto<UserProfileResponseDto> getUserProfile(
        @UserId Long userId,
        @PathVariable Long profileUserId
    ) {
        return ApiResponseDto.success(SuccessStatus.GET_USER_PROFILE_SUCCESS,
            userService.getUserProfile(profileUserId, userId));
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponseDto<DeleteUserResponseDto> deleteUser(
            @UserId Long userId,
            @RequestHeader(required = false) @NotBlank String appleAccessToken
    ){
        return ApiResponseDto.success(SuccessStatus.DELETE_USER_SUCCESS, userService.deleteUser(userId,appleAccessToken));
    }

}
