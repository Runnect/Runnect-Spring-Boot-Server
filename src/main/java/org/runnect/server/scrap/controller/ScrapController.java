package org.runnect.server.scrap.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.runnect.server.common.dto.ApiResponseDto;
import org.runnect.server.common.constant.SuccessStatus;
import org.runnect.server.common.resolver.userId.UserId;
import org.runnect.server.scrap.dto.request.CreateAndDeleteScrapRequestDto;
import org.runnect.server.scrap.dto.response.GetScrapCourseResponseDto;
import org.runnect.server.scrap.service.ScrapService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Scrap", description = "Scrap API Document")
public class ScrapController {
    private final ScrapService scrapService;
    @PostMapping("scrap")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "createAndDeleteScrap", description = "코스 스크랩, 스크랩 취소")
    public ApiResponseDto createAndDeleteScrap(@UserId Long userId, @RequestBody @Valid final CreateAndDeleteScrapRequestDto request) {

        if (request.getScrapTF() == true) {
            return ApiResponseDto.success(SuccessStatus.CREATE_SCRAP_SUCCESS, scrapService.createAndDeleteScrap(userId, request));
        }
        return ApiResponseDto.success(SuccessStatus.DELETE_SCRAP_SUCCESS, scrapService.createAndDeleteScrap(userId, request));
    }

    @GetMapping("scrap/user")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "getScrapCourseByUser", description = "내가 스크랩한 코스")
    public ApiResponseDto<GetScrapCourseResponseDto> getScrapCourseByUser(@UserId Long userId) {
        return ApiResponseDto.success(SuccessStatus.GET_SCRAP_COURSE_BY_USER_SUCCESS, scrapService.getScrapCourseByUser(userId));
    }
}
