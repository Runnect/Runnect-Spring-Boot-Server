package org.runnect.server.record.controller;

import javax.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.runnect.server.common.constant.SuccessStatus;
import org.runnect.server.common.dto.ApiResponseDto;
import org.runnect.server.common.resolver.userId.UserId;
import org.runnect.server.record.dto.request.CreateRecordRequestDto;
import org.runnect.server.record.dto.request.DeleteRecordsRequestDto;
import org.runnect.server.record.dto.request.UpdateRecordRequestDto;
import org.runnect.server.record.dto.response.CreateRecordResponseDto;
import org.runnect.server.record.dto.response.DeleteRecordsResponseDto;
import org.runnect.server.record.dto.response.GetRecordResponseDto;
import org.runnect.server.record.dto.response.UpdateRecordResponseDto;
import org.runnect.server.record.service.RecordService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Record", description = "Record API Document")
public class RecordController {
    private final RecordService recordService;

    @PostMapping("record")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "createRecord", description = "러닝기록하기")
    public ApiResponseDto<CreateRecordResponseDto> createRecord(@UserId Long userId, @RequestBody @Valid final CreateRecordRequestDto request) {

        return ApiResponseDto.success(SuccessStatus.CREATE_RECORD_SUCCESS, recordService.createRecord(userId, request));
    }

    @GetMapping("record/user")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "getRecordByUser", description = "유저 러닝 기록")
    public ApiResponseDto<GetRecordResponseDto> getRecordByUser(@UserId Long userId) {
        return ApiResponseDto.success(SuccessStatus.GET_RECORD_SUCCESS, recordService.getRecordByUser(userId));
    }

    @PatchMapping("record/{recordId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "updateRecord", description = "러닝기록 제목 수정")
    public ApiResponseDto<UpdateRecordResponseDto> updateRecord(@UserId Long userId, @PathVariable(name = "recordId") Long recordId, @RequestBody @Valid final UpdateRecordRequestDto request) {
        return ApiResponseDto.success(SuccessStatus.UPDATE_RECORD_SUCCESS, recordService.updateRecord(userId, recordId, request));
    }

    @PutMapping("record")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "deleteRecord", description = "러닝기록삭제")
    public ApiResponseDto<DeleteRecordsResponseDto> deleteRecords(
        @UserId Long userId,
        @Valid @RequestBody DeleteRecordsRequestDto requestDto
    ) {
        return ApiResponseDto.success(
            SuccessStatus.DELETE_RECORD_SUCCESS, recordService.deleteRecords(userId, requestDto)
        );
    }

}
