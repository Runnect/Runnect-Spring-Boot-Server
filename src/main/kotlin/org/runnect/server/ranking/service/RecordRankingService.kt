package org.runnect.server.ranking.service

import org.runnect.server.ranking.dto.MyRankingResponse
import org.runnect.server.ranking.dto.RankingEntryResponse
import org.runnect.server.ranking.dto.RankingListResponse
import org.runnect.server.record.repository.RecordRepository
import org.springframework.data.redis.connection.RedisZSetCommands.ZAddArgs
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.sql.Time

/**
 * 코스별 완주 기록 랭킹.
 *
 * 랭킹 정렬 기준은 Redis Sorted Set(ranking:record:{courseId})이 유일한 소스다.
 * ZADD 옵션 LT(기존 score보다 작을 때만 반영) + CH(실제 변경 여부 반환)를 사용해
 * "동시에 들어온 완주 기록 중 개인 최고기록만 남기는" 비교-후-갱신을 Redis 내부에서
 * 원자적으로 처리한다. 애플리케이션에서 GET → 비교 → SET을 나눠서 하면 그 사이에
 * 동시 요청이 끼어드는 Lost Update 창이 생기는데, ZADD LT는 그 창을 없앤다.
 *
 * userId → recordId 매핑(record 상세를 다시 읽기 위한 보조 인덱스)은 별도 Hash에
 * eventual하게 둔다. 랭킹 score 자체의 정합성과는 무관한 부가 정보라 원자성이 필요 없다.
 */
@Service
class RecordRankingService(
    private val stringRedisTemplate: StringRedisTemplate,
    private val recordRepository: RecordRepository,
) {

    fun updateBestRecord(courseId: Long, userId: Long, recordId: Long, time: Time): Boolean {
        val timeSeconds = time.toLocalTime().toSecondOfDay().toDouble()

        val updated = stringRedisTemplate.execute { connection ->
            connection.zAdd(
                rankingKey(courseId).toByteArray(StandardCharsets.UTF_8),
                timeSeconds,
                userId.toString().toByteArray(StandardCharsets.UTF_8),
                ZAddArgs.empty().lt().ch(),
            )
        } ?: false

        if (updated) {
            stringRedisTemplate.opsForHash<String, String>()
                .put(recordIndexKey(courseId), userId.toString(), recordId.toString())
        }

        return updated
    }

    /**
     * 이 랭킹 기능이 배포되기 전에 이미 쌓여있던 완주 기록을 Redis로 백필한다.
     * 신규 기능 배포 시 1회성으로 실행하는 운영 작업 — 몇 번을 다시 돌려도
     * ZADD LT가 "더 느린 기록이면 무시"하므로 안전하다(idempotent).
     */
    fun backfillAll(): Int {
        val records = recordRepository.findAllByPublicCourseIsNotNull()
        return records.count { record ->
            updateBestRecord(record.publicCourse.id, record.runnectUser.id, record.id, record.time)
        }
    }

    fun getRanking(courseId: Long, limit: Long): RankingListResponse {
        val zSetOps = stringRedisTemplate.opsForZSet()
        val totalCount = zSetOps.zCard(rankingKey(courseId)) ?: 0L
        val topTuples = zSetOps.rangeWithScores(rankingKey(courseId), 0, limit - 1) ?: emptySet()

        val userIds = topTuples.mapNotNull { it.value?.toLongOrNull() }
        val recordIdByUserId = fetchRecordIdsByUserId(courseId, userIds)
        val recordsById = recordRepository.findByIdIn(recordIdByUserId.values.mapNotNull { it?.toLongOrNull() })
            .associateBy { it.id }

        val entries = topTuples.mapIndexedNotNull { index, tuple ->
            val userId = tuple.value?.toLongOrNull() ?: return@mapIndexedNotNull null
            val recordId = recordIdByUserId[userId.toString()]?.toLongOrNull() ?: return@mapIndexedNotNull null
            val record = recordsById[recordId] ?: return@mapIndexedNotNull null

            RankingEntryResponse.of(rank = index + 1, userId = userId, record = record)
        }

        return RankingListResponse(totalCount = totalCount, entries = entries)
    }

    fun getMyRanking(courseId: Long, userId: Long): MyRankingResponse {
        val rank = stringRedisTemplate.opsForZSet().rank(rankingKey(courseId), userId.toString())
            ?: return MyRankingResponse.notFound(userId)
        val recordId = stringRedisTemplate.opsForHash<String, String>()
            .get(recordIndexKey(courseId), userId.toString())
            ?.toLongOrNull() ?: return MyRankingResponse.notFound(userId)
        val record = recordRepository.findById(recordId).orElse(null)
            ?: return MyRankingResponse.notFound(userId)

        return MyRankingResponse.of(rank = rank.toInt() + 1, userId = userId, record = record)
    }

    private fun fetchRecordIdsByUserId(courseId: Long, userIds: List<Long>): Map<String, String?> {
        if (userIds.isEmpty()) return emptyMap()

        val fields = userIds.map { it.toString() }
        val values = stringRedisTemplate.opsForHash<String, String>().multiGet(recordIndexKey(courseId), fields)
        return fields.zip(values).toMap()
    }

    private fun rankingKey(courseId: Long) = "ranking:record:$courseId"
    private fun recordIndexKey(courseId: Long) = "ranking:record:$courseId:record"
}
