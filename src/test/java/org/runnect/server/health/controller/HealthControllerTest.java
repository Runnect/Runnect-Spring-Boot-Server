package org.runnect.server.health.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.runnect.server.common.constant.ErrorStatus;
import org.runnect.server.common.constant.TokenStatus;
import org.runnect.server.common.exception.ConflictException;
import org.runnect.server.common.exception.PermissionDeniedException;
import org.runnect.server.config.jwt.JwtService;
import org.runnect.server.config.slack.SlackApi;
import org.runnect.server.health.dto.request.HealthDataRequestDto;
import org.runnect.server.health.dto.response.CreateHealthDataResponseDto;
import org.runnect.server.health.dto.response.GetHealthDataResponseDto;
import org.runnect.server.health.dto.response.GetHealthSummaryResponseDto;
import org.runnect.server.health.service.HealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(HealthController.class)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private HealthService healthService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private SlackApi slackApi;

    @BeforeEach
    void setUpAuth() {
        BDDMockito.lenient().when(jwtService.verifyToken("valid")).thenReturn(TokenStatus.TOKEN_VALID);
        BDDMockito.lenient().when(jwtService.getJwtContents("valid")).thenReturn("1");
    }

    private <B extends MockHttpServletRequestBuilder> B withAuth(B builder) {
        builder.header("accessToken", "valid").header("refreshToken", "valid");
        return builder;
    }

    private HealthDataRequestDto validRequest() {
        return new HealthDataRequestDto(150.0, 180.0, 100.0, 320.0, 60, 120, 90, 30, 10, 190.0, Collections.emptyList());
    }

    @Nested
    @DisplayName("POST /api/record/{recordId}/health")
    class CreateHealthData {

        @Test
        @DisplayName("정상 요청이면 201과 생성된 건강 데이터 id를 반환한다")
        void 정상_생성() throws Exception {
            when(healthService.createHealthData(eq(1L), eq(10L), any()))
                .thenReturn(CreateHealthDataResponseDto.of(100L));

            mockMvc.perform(withAuth(post("/api/record/10/health"))
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.healthDataId").value(100));
        }

        @Test
        @DisplayName("필수값(avgHeartRate)이 없으면 400을 반환한다")
        void 필수값_없음() throws Exception {
            mockMvc.perform(withAuth(post("/api/record/10/health"))
                    .contentType("application/json")
                    .content("{\"calories\":320.0,\"zone1Seconds\":1,\"zone2Seconds\":1,\"zone3Seconds\":1,\"zone4Seconds\":1,\"zone5Seconds\":1}"))
                .andExpect(status().isBadRequest());

            BDDMockito.verifyNoInteractions(healthService);
        }

        @Test
        @DisplayName("다른 유저의 레코드면 403을 반환한다")
        void 소유권_없음() throws Exception {
            when(healthService.createHealthData(eq(1L), eq(10L), any()))
                .thenThrow(new PermissionDeniedException(
                    ErrorStatus.PERMISSION_DENIED_HEALTH_DATA_EXCEPTION,
                    ErrorStatus.PERMISSION_DENIED_HEALTH_DATA_EXCEPTION.getMessage()));

            mockMvc.perform(withAuth(post("/api/record/10/health"))
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("이미 건강 데이터가 있으면 409를 반환한다")
        void 이미_존재() throws Exception {
            when(healthService.createHealthData(eq(1L), eq(10L), any()))
                .thenThrow(new ConflictException(
                    ErrorStatus.ALREADY_EXIST_HEALTH_DATA_EXCEPTION,
                    ErrorStatus.ALREADY_EXIST_HEALTH_DATA_EXCEPTION.getMessage()));

            mockMvc.perform(withAuth(post("/api/record/10/health"))
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("GET /api/record/{recordId}/health")
    class GetHealthData {

        @Test
        @DisplayName("정상 요청이면 200과 건강 데이터를 반환한다")
        void 정상_조회() throws Exception {
            GetHealthDataResponseDto.ZoneResponse zones =
                GetHealthDataResponseDto.ZoneResponse.of(60, 120, 90, 30, 10);
            GetHealthDataResponseDto response = GetHealthDataResponseDto.of(
                GetHealthDataResponseDto.HealthDataDetailResponse.of(
                    100L, 10L, 150.0, 180.0, 100.0, 320.0, zones, 190.0, Collections.emptyList()));
            when(healthService.getHealthData(anyLong(), eq(10L))).thenReturn(response);

            mockMvc.perform(withAuth(get("/api/record/10/health")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.healthData.id").value(100));
        }
    }

    @Nested
    @DisplayName("GET /api/health/summary")
    class GetHealthSummary {

        @Test
        @DisplayName("정상 요청이면 200과 요약 데이터를 반환한다")
        void 정상_조회() throws Exception {
            GetHealthSummaryResponseDto response = GetHealthSummaryResponseDto.of(
                GetHealthSummaryResponseDto.HealthSummaryResponse.of(
                    10L, 5L, 150.0, 300.0, 1500.0,
                    GetHealthDataResponseDto.ZoneResponse.of(60, 120, 90, 30, 10)));
            when(healthService.getHealthSummary(1L, "2026-01-01", "2026-01-31")).thenReturn(response);

            mockMvc.perform(withAuth(get("/api/health/summary")
                    .param("startDate", "2026-01-01")
                    .param("endDate", "2026-01-31")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summary.totalRecords").value(10));
        }

        @Test
        @DisplayName("필수 파라미터가 없으면 400을 반환한다")
        void 파라미터_없음() throws Exception {
            mockMvc.perform(withAuth(get("/api/health/summary").param("startDate", "2026-01-01")))
                .andExpect(status().isBadRequest());

            BDDMockito.verifyNoInteractions(healthService);
        }
    }

    @Nested
    @DisplayName("DELETE /api/record/{recordId}/health")
    class DeleteHealthData {

        @Test
        @DisplayName("정상 요청이면 200을 반환한다")
        void 정상_삭제() throws Exception {
            mockMvc.perform(withAuth(delete("/api/record/10/health")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        }
    }
}
