package org.runnect.server.record.controller;

import lombok.RequiredArgsConstructor;
import org.runnect.server.common.dto.ApiResponseDto;
import org.runnect.server.common.exception.SuccessStatus;
import org.runnect.server.record.dto.request.CreateRecordRequestDto;
import org.runnect.server.record.dto.response.CreateRecordDto;
import org.runnect.server.record.dto.response.CreateRecordResponseDto;
import org.runnect.server.record.service.RecordService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RecordController {
    private final RecordService recordService;

    @PostMapping("record")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponseDto<CreateRecordResponseDto> createRecord(@RequestHeader Long userId, @RequestBody @Valid final CreateRecordRequestDto request) {

        return ApiResponseDto.success(SuccessStatus.CREATE_RECORD_SUCCESS, recordService.createRecord(userId, request));
    }

}
