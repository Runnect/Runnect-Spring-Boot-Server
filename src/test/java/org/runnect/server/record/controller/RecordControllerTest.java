package org.runnect.server.record.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.runnect.server.common.constant.TokenStatus;
import org.runnect.server.config.jwt.JwtService;
import org.runnect.server.config.slack.SlackApi;
import org.runnect.server.record.dto.response.CreateRecordDto;
import org.runnect.server.record.dto.response.CreateRecordResponseDto;
import org.runnect.server.record.dto.response.DeleteRecordsResponseDto;
import org.runnect.server.record.dto.response.GetRecordResponseDto;
import org.runnect.server.record.dto.response.UpdateRecordResponse;
import org.runnect.server.record.dto.response.UpdateRecordResponseDto;
import org.runnect.server.record.dto.response.UserResponse;
import org.runnect.server.record.service.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(RecordController.class)
class RecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecordService recordService;
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

    @Nested
    @DisplayName("POST /api/record")
    class CreateRecord {

        @Test
        @DisplayName("정상 요청이면 201과 생성된 레코드 정보를 반환한다")
        void 정상_생성() throws Exception {
            when(recordService.createRecord(eq(1L), any()))
                .thenReturn(new CreateRecordResponseDto(new CreateRecordDto(100L, "2026-01-01T00:00:00")));

            mockMvc.perform(withAuth(post("/api/record"))
                    .contentType("application/json")
                    .content("{\"courseId\":10,\"title\":\"정왕역 코스\",\"time\":\"00:30:00\",\"pace\":\"6'00\\\"\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.record.id").value(100));
        }

        @Test
        @DisplayName("title이 없으면 400을 반환한다")
        void 제목_없음() throws Exception {
            mockMvc.perform(withAuth(post("/api/record"))
                    .contentType("application/json")
                    .content("{\"courseId\":10,\"time\":\"00:30:00\",\"pace\":\"6'00\\\"\"}"))
                .andExpect(status().isBadRequest());

            BDDMockito.verifyNoInteractions(recordService);
        }
    }

    @Nested
    @DisplayName("GET /api/record/user")
    class GetRecordByUser {

        @Test
        @DisplayName("정상 요청이면 200과 레코드 목록을 반환한다")
        void 정상_조회() throws Exception {
            when(recordService.getRecordByUser(1L))
                .thenReturn(GetRecordResponseDto.of(UserResponse.of(1L), Collections.emptyList()));

            mockMvc.perform(withAuth(get("/api/record/user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.userId").value(1));
        }
    }

    @Nested
    @DisplayName("PATCH /api/record/{recordId}")
    class UpdateRecord {

        @Test
        @DisplayName("정상 요청이면 200과 수정된 제목을 반환한다")
        void 정상_수정() throws Exception {
            when(recordService.updateRecord(eq(1L), eq(10L), any()))
                .thenReturn(UpdateRecordResponseDto.of(UpdateRecordResponse.of(10L, "새 제목")));

            mockMvc.perform(withAuth(patch("/api/record/10"))
                    .contentType("application/json")
                    .content("{\"title\":\"새 제목\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.record.title").value("새 제목"));
        }

        @Test
        @DisplayName("title이 없으면 400을 반환한다")
        void 제목_없음() throws Exception {
            mockMvc.perform(withAuth(patch("/api/record/10"))
                    .contentType("application/json")
                    .content("{\"title\":\"\"}"))
                .andExpect(status().isBadRequest());

            BDDMockito.verifyNoInteractions(recordService);
        }
    }

    @Nested
    @DisplayName("PUT /api/record")
    class DeleteRecords {

        @Test
        @DisplayName("정상 요청이면 200과 삭제 개수를 반환한다")
        void 정상_삭제() throws Exception {
            when(recordService.deleteRecords(eq(1L), any())).thenReturn(DeleteRecordsResponseDto.from(1L));

            mockMvc.perform(withAuth(put("/api/record"))
                    .contentType("application/json")
                    .content("{\"recordIdList\":[10]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deletedRecordIdCount").value(1));
        }

        @Test
        @DisplayName("recordIdList가 비어있으면 400을 반환한다")
        void 목록_비어있음() throws Exception {
            mockMvc.perform(withAuth(put("/api/record"))
                    .contentType("application/json")
                    .content("{\"recordIdList\":[]}"))
                .andExpect(status().isBadRequest());

            BDDMockito.verifyNoInteractions(recordService);
        }
    }
}
