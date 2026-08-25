package org.runnect.server.markerquota.controller;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.runnect.server.common.constant.SuccessStatus;
import org.runnect.server.common.dto.ApiResponseDto;
import org.runnect.server.common.resolver.userId.UserId;
import org.runnect.server.markerquota.dto.request.ConsumeMarkerQuotaRequestDto;
import org.runnect.server.markerquota.dto.request.GrantMarkerRewardRequestDto;
import org.runnect.server.markerquota.dto.response.GrantMarkerRewardResponseDto;
import org.runnect.server.markerquota.dto.response.MarkerQuotaResponseDto;
import org.runnect.server.markerquota.service.MarkerQuotaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/marker-quota")
public class MarkerQuotaController {

    private final MarkerQuotaService markerQuotaService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponseDto<MarkerQuotaResponseDto> getBalance(@UserId Long userId) {
        return ApiResponseDto.success(SuccessStatus.GET_MARKER_QUOTA_SUCCESS,
            MarkerQuotaResponseDto.of(markerQuotaService.getBalance(userId)));
    }

    @PostMapping("/consume")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponseDto<MarkerQuotaResponseDto> consume(
            @UserId Long userId,
            @RequestBody @Valid ConsumeMarkerQuotaRequestDto request) {
        return ApiResponseDto.success(SuccessStatus.CONSUME_MARKER_QUOTA_SUCCESS,
            MarkerQuotaResponseDto.of(markerQuotaService.consume(userId, request.getAmount())));
    }

    @PostMapping("/reward")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponseDto<GrantMarkerRewardResponseDto> grantFromAd(
            @UserId Long userId,
            @RequestBody @Valid GrantMarkerRewardRequestDto request) {
        return ApiResponseDto.success(SuccessStatus.GRANT_MARKER_REWARD_SUCCESS,
            markerQuotaService.grantFromAd(userId, request.getAmount(), request.getRewardTransactionId()));
    }
}
