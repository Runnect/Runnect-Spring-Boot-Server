package org.runnect.server.ranking.controller

import org.runnect.server.common.constant.SuccessStatus
import org.runnect.server.common.dto.ApiResponseDto
import org.runnect.server.common.resolver.userId.UserId
import org.runnect.server.ranking.service.RecordRankingService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class RecordRankingController(
    private val recordRankingService: RecordRankingService,
) {

    @GetMapping("course/{courseId}/ranking")
    @ResponseStatus(HttpStatus.OK)
    fun getRanking(
        @PathVariable courseId: Long,
        @RequestParam(defaultValue = "20") limit: Long,
    ) = ApiResponseDto.success(
        SuccessStatus.GET_RECORD_RANKING_SUCCESS,
        recordRankingService.getRanking(courseId, limit),
    )

    @GetMapping("course/{courseId}/ranking/me")
    @ResponseStatus(HttpStatus.OK)
    fun getMyRanking(
        @UserId userId: Long,
        @PathVariable courseId: Long,
    ) = ApiResponseDto.success(
        SuccessStatus.GET_MY_RECORD_RANKING_SUCCESS,
        recordRankingService.getMyRanking(courseId, userId),
    )
}
