package org.runnect.server.ranking.dto

import org.runnect.server.record.entity.Record

data class RankingEntryResponse(
    val rank: Int,
    val userId: Long,
    val nickname: String,
    val recordId: Long,
    val time: String,
    val pace: String,
) {
    companion object {
        fun of(rank: Int, userId: Long, record: Record) = RankingEntryResponse(
            rank = rank,
            userId = userId,
            nickname = record.runnectUser.nickname,
            recordId = record.id,
            time = record.time.toString(),
            pace = record.pace.toString(),
        )
    }
}

data class RankingListResponse(
    val totalCount: Long,
    val entries: List<RankingEntryResponse>,
)

// data가 null이면 클라이언트(ResultCall)에서 "null body = 에러"로 취급하기 때문에,
// "아직 이 코스를 완주한 기록이 없음"도 hasRecord=false인 정상 200 응답으로 표현한다.
data class MyRankingResponse(
    val hasRecord: Boolean,
    val rank: Int?,
    val userId: Long,
    val nickname: String?,
    val recordId: Long?,
    val time: String?,
    val pace: String?,
) {
    companion object {
        fun of(rank: Int, userId: Long, record: Record) = MyRankingResponse(
            hasRecord = true,
            rank = rank,
            userId = userId,
            nickname = record.runnectUser.nickname,
            recordId = record.id,
            time = record.time.toString(),
            pace = record.pace.toString(),
        )

        fun notFound(userId: Long) = MyRankingResponse(
            hasRecord = false,
            rank = null,
            userId = userId,
            nickname = null,
            recordId = null,
            time = null,
            pace = null,
        )
    }
}
