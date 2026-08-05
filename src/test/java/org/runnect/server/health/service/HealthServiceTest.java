package org.runnect.server.health.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.runnect.server.common.exception.BadRequestException;
import org.runnect.server.common.exception.ConflictException;
import org.runnect.server.common.exception.NotFoundException;
import org.runnect.server.common.exception.PermissionDeniedException;
import org.runnect.server.health.dto.request.HealthDataRequestDto;
import org.runnect.server.health.dto.request.HeartRateSampleRequestDto;
import org.runnect.server.health.dto.response.CreateHealthDataResponseDto;
import org.runnect.server.health.dto.response.GetHealthDataResponseDto;
import org.runnect.server.health.dto.response.GetHealthSummaryResponseDto;
import org.runnect.server.health.entity.RecordHealthData;
import org.runnect.server.health.repository.RecordHealthDataRepository;
import org.runnect.server.record.entity.Record;
import org.runnect.server.record.repository.RecordRepository;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.entity.SocialType;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class HealthServiceTest {

    @Mock
    private RecordHealthDataRepository recordHealthDataRepository;
    @Mock
    private RecordRepository recordRepository;

    private HealthService healthService;

    @BeforeEach
    void setUp() {
        healthService = new HealthService(recordHealthDataRepository, recordRepository);
    }

    private RunnectUser buildUser(Long id) {
        RunnectUser user = RunnectUser.builder()
            .nickname("러너" + id)
            .socialId("social-" + id)
            .email("user" + id + "@runnect.io")
            .provider(SocialType.KAKAO)
            .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Record buildRecord(Long id, RunnectUser owner) {
        Record record = Record.builder()
            .runnectUser(owner)
            .title("퇴근길 러닝")
            .pace(java.sql.Time.valueOf("00:05:30"))
            .time(java.sql.Time.valueOf("00:25:00"))
            .build();
        ReflectionTestUtils.setField(record, "id", id);
        return record;
    }

    private HealthDataRequestDto healthDataRequestDto(Double avgHeartRate, Double calories,
        List<HeartRateSampleRequestDto> samples) {
        return new HealthDataRequestDto(avgHeartRate, 180.0, 100.0, calories, 60, 120, 90, 30, 0, null, samples);
    }

    @Nested
    @DisplayName("createHealthData")
    class CreateHealthData {

        @Test
        @DisplayName("정상 요청이면 건강 데이터를 생성한다 (샘플 없음)")
        void 정상_생성_샘플없음() {
            RunnectUser user = buildUser(1L);
            Record record = buildRecord(50L, user);
            when(recordRepository.findById(50L)).thenReturn(Optional.of(record));
            when(recordHealthDataRepository.existsByRecordId(50L)).thenReturn(false);

            CreateHealthDataResponseDto response = healthService.createHealthData(1L, 50L,
                healthDataRequestDto(150.0, 300.0, null));

            assertThat(response.getHealthDataId()).isNull();
            verify(recordHealthDataRepository).save(any(RecordHealthData.class));
        }

        @Test
        @DisplayName("심박수 샘플이 있으면 함께 저장된다")
        void 정상_생성_샘플있음() {
            RunnectUser user = buildUser(1L);
            Record record = buildRecord(50L, user);
            when(recordRepository.findById(50L)).thenReturn(Optional.of(record));
            when(recordHealthDataRepository.existsByRecordId(50L)).thenReturn(false);

            List<HeartRateSampleRequestDto> samples = Arrays.asList(
                new HeartRateSampleRequestDto(140.0, 10, 2),
                new HeartRateSampleRequestDto(150.0, 20, 3));

            healthService.createHealthData(1L, 50L, healthDataRequestDto(150.0, 300.0, samples));

            org.mockito.ArgumentCaptor<RecordHealthData> captor =
                org.mockito.ArgumentCaptor.forClass(RecordHealthData.class);
            verify(recordHealthDataRepository).save(captor.capture());
            assertThat(captor.getValue().getHeartRateSamples()).hasSize(2);
        }

        @Test
        @DisplayName("maxHeartRateConfig를 안 보내면 기본값(190.0)이 사용된다")
        void 기본_최대심박수_사용() {
            RunnectUser user = buildUser(1L);
            Record record = buildRecord(50L, user);
            when(recordRepository.findById(50L)).thenReturn(Optional.of(record));
            when(recordHealthDataRepository.existsByRecordId(50L)).thenReturn(false);

            healthService.createHealthData(1L, 50L, healthDataRequestDto(150.0, 300.0, null));

            org.mockito.ArgumentCaptor<RecordHealthData> captor =
                org.mockito.ArgumentCaptor.forClass(RecordHealthData.class);
            verify(recordHealthDataRepository).save(captor.capture());
            assertThat(captor.getValue().getMaxHeartRateConfig()).isEqualTo(190.0);
        }

        @Test
        @DisplayName("존재하지 않는 레코드면 NotFoundException")
        void 존재하지_않는_레코드() {
            when(recordRepository.findById(50L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> healthService.createHealthData(1L, 50L,
                healthDataRequestDto(150.0, 300.0, null)))
                .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("본인 기록이 아니면 PermissionDeniedException")
        void 소유자가_아님() {
            RunnectUser owner = buildUser(1L);
            Record record = buildRecord(50L, owner);
            when(recordRepository.findById(50L)).thenReturn(Optional.of(record));

            assertThatThrownBy(() -> healthService.createHealthData(999L, 50L,
                healthDataRequestDto(150.0, 300.0, null)))
                .isInstanceOf(PermissionDeniedException.class);
        }

        @Test
        @DisplayName("평균 심박수가 0 이하면 BadRequestException")
        void 평균심박수_0이하() {
            RunnectUser user = buildUser(1L);
            Record record = buildRecord(50L, user);
            when(recordRepository.findById(50L)).thenReturn(Optional.of(record));

            assertThatThrownBy(() -> healthService.createHealthData(1L, 50L,
                healthDataRequestDto(0.0, 300.0, null)))
                .isInstanceOf(BadRequestException.class);

            verify(recordHealthDataRepository, never()).save(any());
        }

        @Test
        @DisplayName("칼로리가 음수면 BadRequestException")
        void 칼로리_음수() {
            RunnectUser user = buildUser(1L);
            Record record = buildRecord(50L, user);
            when(recordRepository.findById(50L)).thenReturn(Optional.of(record));

            assertThatThrownBy(() -> healthService.createHealthData(1L, 50L,
                healthDataRequestDto(150.0, -1.0, null)))
                .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("심박수 샘플이 5000건을 초과하면 BadRequestException")
        void 심박수_샘플_초과() {
            RunnectUser user = buildUser(1L);
            Record record = buildRecord(50L, user);
            when(recordRepository.findById(50L)).thenReturn(Optional.of(record));

            List<HeartRateSampleRequestDto> tooMany = java.util.stream.Stream
                .generate(() -> new HeartRateSampleRequestDto(140.0, 1, 2))
                .limit(5001)
                .collect(java.util.stream.Collectors.toList());

            assertThatThrownBy(() -> healthService.createHealthData(1L, 50L,
                healthDataRequestDto(150.0, 300.0, tooMany)))
                .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("이미 건강 데이터가 존재하면 ConflictException")
        void 이미_존재하는_건강데이터() {
            RunnectUser user = buildUser(1L);
            Record record = buildRecord(50L, user);
            when(recordRepository.findById(50L)).thenReturn(Optional.of(record));
            when(recordHealthDataRepository.existsByRecordId(50L)).thenReturn(true);

            assertThatThrownBy(() -> healthService.createHealthData(1L, 50L,
                healthDataRequestDto(150.0, 300.0, null)))
                .isInstanceOf(ConflictException.class);
        }

        @Test
        @DisplayName("동시 요청으로 인한 유니크 제약 위반은 ConflictException으로 변환된다")
        void 동시성_경쟁으로_인한_제약위반() {
            RunnectUser user = buildUser(1L);
            Record record = buildRecord(50L, user);
            when(recordRepository.findById(50L)).thenReturn(Optional.of(record));
            when(recordHealthDataRepository.existsByRecordId(50L)).thenReturn(false);
            doThrow(new DataIntegrityViolationException("duplicate"))
                .when(recordHealthDataRepository).save(any(RecordHealthData.class));

            assertThatThrownBy(() -> healthService.createHealthData(1L, 50L,
                healthDataRequestDto(150.0, 300.0, null)))
                .isInstanceOf(ConflictException.class);
        }
    }

    @Nested
    @DisplayName("getHealthData")
    class GetHealthData {

        @Test
        @DisplayName("건강 데이터가 있으면 상세 정보를 반환한다")
        void 정상_조회() {
            RunnectUser user = buildUser(1L);
            Record record = buildRecord(50L, user);
            RecordHealthData healthData = RecordHealthData.builder()
                .record(record)
                .avgHeartRate(150.0)
                .maxHeartRate(180.0)
                .minHeartRate(100.0)
                .calories(300.0)
                .zone1Seconds(60)
                .zone2Seconds(120)
                .zone3Seconds(90)
                .zone4Seconds(30)
                .zone5Seconds(0)
                .maxHeartRateConfig(190.0)
                .build();
            ReflectionTestUtils.setField(healthData, "id", 200L);

            when(recordRepository.findById(50L)).thenReturn(Optional.of(record));
            when(recordHealthDataRepository.findByRecordIdWithSamples(50L)).thenReturn(Optional.of(healthData));

            GetHealthDataResponseDto response = healthService.getHealthData(1L, 50L);

            assertThat(response.getHealthData().getId()).isEqualTo(200L);
            assertThat(response.getHealthData().getAvgHeartRate()).isEqualTo(150.0);
            assertThat(response.getHealthData().getHeartRateSamples()).isEmpty();
        }

        @Test
        @DisplayName("건강 데이터가 없으면 404가 아니라 healthData가 null인 응답을 반환한다")
        void 건강데이터_없음() {
            RunnectUser user = buildUser(1L);
            Record record = buildRecord(50L, user);
            when(recordRepository.findById(50L)).thenReturn(Optional.of(record));
            when(recordHealthDataRepository.findByRecordIdWithSamples(50L)).thenReturn(Optional.empty());

            GetHealthDataResponseDto response = healthService.getHealthData(1L, 50L);

            assertThat(response.getHealthData()).isNull();
        }

        @Test
        @DisplayName("존재하지 않는 레코드면 NotFoundException")
        void 존재하지_않는_레코드() {
            when(recordRepository.findById(50L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> healthService.getHealthData(1L, 50L))
                .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("본인 기록이 아니면 PermissionDeniedException")
        void 소유자가_아님() {
            RunnectUser owner = buildUser(1L);
            Record record = buildRecord(50L, owner);
            when(recordRepository.findById(50L)).thenReturn(Optional.of(record));

            assertThatThrownBy(() -> healthService.getHealthData(999L, 50L))
                .isInstanceOf(PermissionDeniedException.class);
        }
    }

    @Nested
    @DisplayName("getHealthSummary")
    class GetHealthSummary {

        @Test
        @DisplayName("정상적인 기간이면 통계를 계산해서 반환한다")
        void 정상_조회() {
            Object[] row = new Object[]{5L, 3L, 145.5, 320.0, 960.0, 60, 120, 90, 30, 0};
            when(recordHealthDataRepository.getHealthSummary(
                org.mockito.ArgumentMatchers.eq(1L), any(), any()))
                .thenReturn(Collections.singletonList(row));

            GetHealthSummaryResponseDto response = healthService.getHealthSummary(1L, "2026-01-01", "2026-01-31");

            assertThat(response.getSummary().getTotalRecords()).isEqualTo(5L);
            assertThat(response.getSummary().getRecordsWithHealth()).isEqualTo(3L);
            assertThat(response.getSummary().getAvgHeartRate()).isEqualTo(145.5);
            assertThat(response.getSummary().getZoneDistribution().getZone2Seconds()).isEqualTo(120);
        }

        @Test
        @DisplayName("집계 결과가 없으면 0/null로 채워진 기본값을 반환한다")
        void 집계_결과_없음() {
            when(recordHealthDataRepository.getHealthSummary(
                org.mockito.ArgumentMatchers.eq(1L), any(), any()))
                .thenReturn(Collections.emptyList());

            GetHealthSummaryResponseDto response = healthService.getHealthSummary(1L, "2026-01-01", "2026-01-31");

            assertThat(response.getSummary().getTotalRecords()).isEqualTo(0L);
            assertThat(response.getSummary().getAvgHeartRate()).isNull();
            assertThat(response.getSummary().getZoneDistribution().getZone1Seconds()).isEqualTo(0);
        }

        @Test
        @DisplayName("날짜 형식이 잘못되면 BadRequestException")
        void 잘못된_날짜형식() {
            assertThatThrownBy(() -> healthService.getHealthSummary(1L, "2026/01/01", "2026-01-31"))
                .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("종료일이 시작일보다 빠르면 BadRequestException")
        void 종료일이_시작일보다_빠름() {
            assertThatThrownBy(() -> healthService.getHealthSummary(1L, "2026-01-31", "2026-01-01"))
                .isInstanceOf(BadRequestException.class);
        }
    }

    @Nested
    @DisplayName("deleteHealthData")
    class DeleteHealthData {

        @Test
        @DisplayName("정상적으로 건강 데이터를 삭제한다")
        void 정상_삭제() {
            RunnectUser user = buildUser(1L);
            Record record = buildRecord(50L, user);
            when(recordRepository.findById(50L)).thenReturn(Optional.of(record));
            when(recordHealthDataRepository.existsByRecordId(50L)).thenReturn(true);

            healthService.deleteHealthData(1L, 50L);

            verify(recordHealthDataRepository).deleteByRecordId(50L);
        }

        @Test
        @DisplayName("존재하지 않는 레코드면 NotFoundException")
        void 존재하지_않는_레코드() {
            when(recordRepository.findById(50L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> healthService.deleteHealthData(1L, 50L))
                .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("본인 기록이 아니면 PermissionDeniedException")
        void 소유자가_아님() {
            RunnectUser owner = buildUser(1L);
            Record record = buildRecord(50L, owner);
            when(recordRepository.findById(50L)).thenReturn(Optional.of(record));

            assertThatThrownBy(() -> healthService.deleteHealthData(999L, 50L))
                .isInstanceOf(PermissionDeniedException.class);
        }

        @Test
        @DisplayName("건강 데이터가 없으면 NotFoundException")
        void 건강데이터_없음() {
            RunnectUser user = buildUser(1L);
            Record record = buildRecord(50L, user);
            when(recordRepository.findById(50L)).thenReturn(Optional.of(record));
            when(recordHealthDataRepository.existsByRecordId(50L)).thenReturn(false);

            assertThatThrownBy(() -> healthService.deleteHealthData(1L, 50L))
                .isInstanceOf(NotFoundException.class);

            verify(recordHealthDataRepository, never()).deleteByRecordId(any());
        }
    }
}
