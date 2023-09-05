package org.runnect.server.scrap.controller;

import lombok.RequiredArgsConstructor;
import org.runnect.server.common.dto.ApiResponseDto;
import org.runnect.server.common.exception.SuccessStatus;
import org.runnect.server.scrap.dto.request.CreateAndDeleteScrapRequestDto;
import org.runnect.server.scrap.service.ScrapService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ScrapController {
    private final ScrapService scrapService;
    @PostMapping("scrap")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponseDto createAndDeleteScrap(@RequestHeader Long userId, @RequestBody @Valid final CreateAndDeleteScrapRequestDto request) {

        scrapService.createAndDeleteScrap(userId, request);

        if (request.getScrapTF() == true) {
            return ApiResponseDto.success(SuccessStatus.CREATE_SCRAP_SUCCESS);
        }
        return ApiResponseDto.success(SuccessStatus.DELETE_SCRAP_SUCCESS);
    }
}
