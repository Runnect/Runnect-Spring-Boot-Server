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
        // UserIdResolver#supportsParameter가 boxed java.lang.Long만 매칭한다.
        // Kotlin의 non-null Long은 바이트코드상 primitive long으로 컴파일되어
        // 리졸버가 파라미터를 아예 인식하지 못하므로, 반드시 nullable(Long?)로 선언해
        // boxed Long으로 컴파일되게 한다. 실제로 null이 들어오는 경우는 없다 —
        // 리졸버가 토큰이 없거나 유효하지 않으면 예외를 던진다.
        @UserId userId: Long?,
        @PathVariable courseId: Long,
    ) = ApiResponseDto.success(
        SuccessStatus.GET_MY_RECORD_RANKING_SUCCESS,
        recordRankingService.getMyRanking(courseId, requireNotNull(userId)),
    )
}
