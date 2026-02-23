package org.runnect.server.health.controller;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.runnect.server.common.constant.SuccessStatus;
import org.runnect.server.common.dto.ApiResponseDto;
import org.runnect.server.common.resolver.userId.UserId;
import org.runnect.server.health.dto.request.HealthDataRequestDto;
import org.runnect.server.health.dto.response.CreateHealthDataResponseDto;
import org.runnect.server.health.dto.response.GetHealthDataResponseDto;
import org.runnect.server.health.dto.response.GetHealthSummaryResponseDto;
import org.runnect.server.health.service.HealthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class HealthController {

    private final HealthService healthService;

    @PostMapping("record/{recordId}/health")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponseDto<CreateHealthDataResponseDto> createHealthData(
            @UserId Long userId,
            @PathVariable(name = "recordId") Long recordId,
            @RequestBody @Valid final HealthDataRequestDto request) {
        return ApiResponseDto.success(SuccessStatus.CREATE_HEALTH_DATA_SUCCESS,
                healthService.createHealthData(userId, recordId, request));
    }

    @GetMapping("record/{recordId}/health")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponseDto<GetHealthDataResponseDto> getHealthData(
            @UserId Long userId,
            @PathVariable(name = "recordId") Long recordId) {
        return ApiResponseDto.success(SuccessStatus.GET_HEALTH_DATA_SUCCESS,
                healthService.getHealthData(userId, recordId));
    }

    @GetMapping("health/summary")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponseDto<GetHealthSummaryResponseDto> getHealthSummary(
            @UserId Long userId,
            @RequestParam(name = "startDate") String startDate,
            @RequestParam(name = "endDate") String endDate) {
        return ApiResponseDto.success(SuccessStatus.GET_HEALTH_SUMMARY_SUCCESS,
                healthService.getHealthSummary(userId, startDate, endDate));
    }

    @DeleteMapping("record/{recordId}/health")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponseDto deleteHealthData(
            @UserId Long userId,
            @PathVariable(name = "recordId") Long recordId) {
        healthService.deleteHealthData(userId, recordId);
        return ApiResponseDto.success(SuccessStatus.DELETE_HEALTH_DATA_SUCCESS);
    }
}
