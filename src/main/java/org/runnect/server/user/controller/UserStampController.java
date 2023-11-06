package org.runnect.server.user.controller;

import static org.runnect.server.common.constant.SuccessStatus.GET_USER_STAMPS_SUCCESS;

import lombok.RequiredArgsConstructor;
import org.runnect.server.common.dto.ApiResponseDto;
import org.runnect.server.common.resolver.userId.UserId;
import org.runnect.server.user.dto.response.GetUserStampsResponseDto;
import org.runnect.server.user.service.UserStampService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stamp")
public class UserStampController {

    private final UserStampService userStampService;

    @GetMapping("/user")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponseDto<GetUserStampsResponseDto> getUserStamps(
        @UserId Long userId
    ) {
        return ApiResponseDto.success(GET_USER_STAMPS_SUCCESS,
            userStampService.findUserStamps(userId));
    }
}
