package org.runnect.server.banner.controller;

import lombok.RequiredArgsConstructor;
import org.runnect.server.banner.dto.response.GetBannerResponseDto;
import org.runnect.server.banner.service.BannerService;
import org.runnect.server.common.constant.SuccessStatus;
import org.runnect.server.common.dto.ApiResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/banner")
public class BannerController {

    private final BannerService bannerService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponseDto<GetBannerResponseDto> getBanners() {
        return ApiResponseDto.success(SuccessStatus.GET_BANNER_SUCCESS, bannerService.getBanners());
    }
}
