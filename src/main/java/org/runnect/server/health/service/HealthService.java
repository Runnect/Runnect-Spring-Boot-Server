package org.runnect.server.health.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.runnect.server.common.constant.ErrorStatus;
import org.runnect.server.common.exception.BadRequestException;
import org.runnect.server.common.exception.ConflictException;
import org.runnect.server.common.exception.NotFoundException;
import org.runnect.server.common.exception.PermissionDeniedException;
import org.runnect.server.health.dto.request.HealthDataRequestDto;
import org.runnect.server.health.dto.request.HeartRateSampleRequestDto;
import org.runnect.server.health.dto.response.CreateHealthDataResponseDto;
import org.runnect.server.health.dto.response.GetHealthDataResponseDto;
import org.runnect.server.health.dto.response.GetHealthDataResponseDto.HealthDataDetailResponse;
import org.runnect.server.health.dto.response.GetHealthDataResponseDto.HeartRateSampleResponse;
import org.runnect.server.health.dto.response.GetHealthDataResponseDto.ZoneResponse;
import org.runnect.server.health.dto.response.GetHealthSummaryResponseDto;
import org.runnect.server.health.dto.response.GetHealthSummaryResponseDto.HealthSummaryResponse;
import org.runnect.server.health.entity.HeartRateSample;
import org.runnect.server.health.entity.RecordHealthData;
import org.runnect.server.health.repository.RecordHealthDataRepository;
import org.runnect.server.record.entity.Record;
import org.runnect.server.record.repository.RecordRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HealthService {

    private final RecordHealthDataRepository recordHealthDataRepository;
    private final RecordRepository recordRepository;

    private static final int MAX_HEART_RATE_SAMPLES = 5000;
    private static final double DEFAULT_MAX_HEART_RATE_CONFIG = 190.0;

    @Transactional
    public CreateHealthDataResponseDto createHealthData(Long userId, Long recordId, HealthDataRequestDto request) {
        // 1. Record 존재 확인
        Record record = recordRepository.findById(recordId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorStatus.NOT_FOUND_RECORD_EXCEPTION,
                        ErrorStatus.NOT_FOUND_RECORD_EXCEPTION.getMessage()));

        // 2. 소유권 확인
        if (!record.getRunnectUser().getId().equals(userId)) {
            throw new PermissionDeniedException(
                    ErrorStatus.PERMISSION_DENIED_HEALTH_DATA_EXCEPTION,
                    ErrorStatus.PERMISSION_DENIED_HEALTH_DATA_EXCEPTION.getMessage());
        }

        // 3. 유효성 검증
        validateHealthData(request);

        // 4. 중복 확인
        if (recordHealthDataRepository.existsByRecordId(recordId)) {
            throw new ConflictException(
                    ErrorStatus.ALREADY_EXIST_HEALTH_DATA_EXCEPTION,
                    ErrorStatus.ALREADY_EXIST_HEALTH_DATA_EXCEPTION.getMessage());
        }

        // 5. RecordHealthData 생성
        Double maxHeartRateConfig = request.getMaxHeartRateConfig() != null
                ? request.getMaxHeartRateConfig() : DEFAULT_MAX_HEART_RATE_CONFIG;

        RecordHealthData healthData = RecordHealthData.builder()
                .record(record)
                .avgHeartRate(request.getAvgHeartRate())
                .maxHeartRate(request.getMaxHeartRate())
                .minHeartRate(request.getMinHeartRate())
                .calories(request.getCalories())
                .zone1Seconds(request.getZone1Seconds())
                .zone2Seconds(request.getZone2Seconds())
                .zone3Seconds(request.getZone3Seconds())
                .zone4Seconds(request.getZone4Seconds())
                .zone5Seconds(request.getZone5Seconds())
                .maxHeartRateConfig(maxHeartRateConfig)
                .build();

        // 6. HeartRateSamples 처리
        if (request.getHeartRateSamples() != null && !request.getHeartRateSamples().isEmpty()) {
            List<HeartRateSample> samples = request.getHeartRateSamples().stream()
                    .map(dto -> HeartRateSample.builder()
                            .heartRate(dto.getHeartRate())
                            .elapsedSeconds(dto.getElapsedSeconds())
                            .zone(dto.getZone())
                            .build())
                    .collect(Collectors.toList());
            healthData.addHeartRateSamples(samples);
        }

        // 7. 저장 (UNIQUE 제약 위반 시 409)
        try {
            recordHealthDataRepository.save(healthData);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(
                    ErrorStatus.ALREADY_EXIST_HEALTH_DATA_EXCEPTION,
                    ErrorStatus.ALREADY_EXIST_HEALTH_DATA_EXCEPTION.getMessage());
        }

        return CreateHealthDataResponseDto.of(healthData.getId());
    }

    @Transactional(readOnly = true)
    public GetHealthDataResponseDto getHealthData(Long userId, Long recordId) {
        // 1. Record 존재 확인
        Record record = recordRepository.findById(recordId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorStatus.NOT_FOUND_RECORD_EXCEPTION,
                        ErrorStatus.NOT_FOUND_RECORD_EXCEPTION.getMessage()));

        // 2. 소유권 확인
        if (!record.getRunnectUser().getId().equals(userId)) {
            throw new PermissionDeniedException(
                    ErrorStatus.PERMISSION_DENIED_HEALTH_DATA_EXCEPTION,
                    ErrorStatus.PERMISSION_DENIED_HEALTH_DATA_EXCEPTION.getMessage());
        }

        // 3. 건강 데이터 조회 (없으면 null 반환, 404가 아님)
        return recordHealthDataRepository.findByRecordIdWithSamples(recordId)
                .map(this::toHealthDataDetailResponse)
                .orElse(GetHealthDataResponseDto.of(null));
    }

    @Transactional(readOnly = true)
    public GetHealthSummaryResponseDto getHealthSummary(Long userId, String startDateStr, String endDateStr) {
        // 1. 날짜 파싱 및 검증
        LocalDate startDate;
        LocalDate endDate;
        try {
            startDate = LocalDate.parse(startDateStr);
            endDate = LocalDate.parse(endDateStr);
        } catch (Exception e) {
            throw new BadRequestException(
                    ErrorStatus.INVALID_DATE_RANGE_EXCEPTION,
                    ErrorStatus.INVALID_DATE_RANGE_EXCEPTION.getMessage());
        }

        if (endDate.isBefore(startDate)) {
            throw new BadRequestException(
                    ErrorStatus.INVALID_DATE_RANGE_EXCEPTION,
                    ErrorStatus.INVALID_DATE_RANGE_EXCEPTION.getMessage());
        }

        // 2. 날짜 범위 변환 (endDate 당일 포함을 위해 다음날 00:00:00 사용)
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        // 3. 통계 쿼리 실행
        Object[] result = recordHealthDataRepository.getHealthSummary(userId, startDateTime, endDateTime);

        Long totalRecords = result[0] != null ? ((Number) result[0]).longValue() : 0L;
        Long recordsWithHealth = result[1] != null ? ((Number) result[1]).longValue() : 0L;
        Double avgHeartRate = result[2] != null ? ((Number) result[2]).doubleValue() : null;
        Double avgCalories = result[3] != null ? ((Number) result[3]).doubleValue() : null;
        Double totalCalories = result[4] != null ? ((Number) result[4]).doubleValue() : null;
        Integer zone1 = result[5] != null ? ((Number) result[5]).intValue() : 0;
        Integer zone2 = result[6] != null ? ((Number) result[6]).intValue() : 0;
        Integer zone3 = result[7] != null ? ((Number) result[7]).intValue() : 0;
        Integer zone4 = result[8] != null ? ((Number) result[8]).intValue() : 0;
        Integer zone5 = result[9] != null ? ((Number) result[9]).intValue() : 0;

        ZoneResponse zoneDistribution = ZoneResponse.of(zone1, zone2, zone3, zone4, zone5);
        HealthSummaryResponse summary = HealthSummaryResponse.of(
                totalRecords, recordsWithHealth, avgHeartRate, avgCalories, totalCalories, zoneDistribution);

        return GetHealthSummaryResponseDto.of(summary);
    }

    @Transactional
    public void deleteHealthData(Long userId, Long recordId) {
        // 1. Record 존재 확인
        Record record = recordRepository.findById(recordId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorStatus.NOT_FOUND_RECORD_EXCEPTION,
                        ErrorStatus.NOT_FOUND_RECORD_EXCEPTION.getMessage()));

        // 2. 소유권 확인
        if (!record.getRunnectUser().getId().equals(userId)) {
            throw new PermissionDeniedException(
                    ErrorStatus.PERMISSION_DENIED_HEALTH_DATA_EXCEPTION,
                    ErrorStatus.PERMISSION_DENIED_HEALTH_DATA_EXCEPTION.getMessage());
        }

        // 3. 건강 데이터 존재 확인
        if (!recordHealthDataRepository.existsByRecordId(recordId)) {
            throw new NotFoundException(
                    ErrorStatus.NOT_FOUND_RECORD_EXCEPTION,
                    ErrorStatus.NOT_FOUND_RECORD_EXCEPTION.getMessage());
        }

        // 4. 삭제 (CASCADE로 samples도 삭제)
        recordHealthDataRepository.deleteByRecordId(recordId);
    }

    private void validateHealthData(HealthDataRequestDto request) {
        if (request.getAvgHeartRate() <= 0) {
            throw new BadRequestException(
                    ErrorStatus.INVALID_HEALTH_DATA_EXCEPTION,
                    ErrorStatus.INVALID_HEALTH_DATA_EXCEPTION.getMessage());
        }

        if (request.getCalories() < 0) {
            throw new BadRequestException(
                    ErrorStatus.INVALID_HEALTH_DATA_EXCEPTION,
                    ErrorStatus.INVALID_HEALTH_DATA_EXCEPTION.getMessage());
        }

        // heartRateSamples 최대 건수 제한
        if (request.getHeartRateSamples() != null && request.getHeartRateSamples().size() > MAX_HEART_RATE_SAMPLES) {
            throw new BadRequestException(
                    ErrorStatus.EXCEED_HEART_RATE_SAMPLES_EXCEPTION,
                    ErrorStatus.EXCEED_HEART_RATE_SAMPLES_EXCEPTION.getMessage());
        }
    }

    private GetHealthDataResponseDto toHealthDataDetailResponse(RecordHealthData healthData) {
        List<HeartRateSampleResponse> sampleResponses = healthData.getHeartRateSamples().stream()
                .map(s -> HeartRateSampleResponse.of(s.getHeartRate(), s.getElapsedSeconds(), s.getZone()))
                .collect(Collectors.toList());

        ZoneResponse zones = ZoneResponse.of(
                healthData.getZone1Seconds(), healthData.getZone2Seconds(),
                healthData.getZone3Seconds(), healthData.getZone4Seconds(),
                healthData.getZone5Seconds());

        HealthDataDetailResponse detail = HealthDataDetailResponse.of(
                healthData.getId(), healthData.getRecord().getId(),
                healthData.getAvgHeartRate(), healthData.getMaxHeartRate(),
                healthData.getMinHeartRate(), healthData.getCalories(),
                zones, healthData.getMaxHeartRateConfig(), sampleResponses);

        return GetHealthDataResponseDto.of(detail);
    }
}
