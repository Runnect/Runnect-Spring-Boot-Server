package org.runnect.server.record.controller;

import lombok.RequiredArgsConstructor;
import org.runnect.server.common.dto.ApiResponseDto;
import org.runnect.server.common.exception.SuccessStatus;
import org.runnect.server.common.resolver.userId.UserId;
import org.runnect.server.record.dto.request.RecordRequestDto;
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
    public ApiResponseDto createRecord(@UserId Integer userId, @RequestBody @Valid final RecordRequestDto request) {
        return ApiResponseDto.success(SuccessStatus.CREATE_RECORD_SUCCESS);
    }

}
