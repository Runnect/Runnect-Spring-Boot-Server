package org.runnect.server.ranking.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.runnect.server.course.entity.Course
import org.runnect.server.publicCourse.entity.PublicCourse
import org.runnect.server.record.entity.Record
import org.runnect.server.record.repository.RecordRepository
import org.runnect.server.user.entity.RunnectUser
import org.runnect.server.user.entity.SocialType
import org.springframework.data.redis.core.DefaultTypedTuple
import org.springframework.data.redis.core.HashOperations
import org.springframework.data.redis.core.RedisCallback
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ZSetOperations
import org.springframework.test.util.ReflectionTestUtils
import java.sql.Time
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class RecordRankingServiceTest {

    private val stringRedisTemplate: StringRedisTemplate = mock(StringRedisTemplate::class.java)
    private val recordRepository: RecordRepository = mock(RecordRepository::class.java)

    private val service = RecordRankingService(stringRedisTemplate, recordRepository)

    @Test
    fun `기존 기록보다 빠르면 hash 인덱스에 recordId가 갱신된다`() {
        val hashOps: HashOperations<String, String, String> = mock(HashOperations::class.java) as HashOperations<String, String, String>
        whenever(stringRedisTemplate.execute(any<RedisCallback<Boolean>>())).thenReturn(true)
        whenever(stringRedisTemplate.opsForHash<String, String>()).thenReturn(hashOps)

        val updated = service.updateBestRecord(1L, 10L, 100L, Time.valueOf("00:25:00"))

        assertThat(updated).isTrue()
        verify(hashOps).put("ranking:record:1:record", "10", "100")
    }

    @Test
    fun `기존 기록보다 느리면 hash 인덱스를 건드리지 않는다`() {
        whenever(stringRedisTemplate.execute(any<RedisCallback<Boolean>>())).thenReturn(false)

        val updated = service.updateBestRecord(1L, 10L, 100L, Time.valueOf("00:30:00"))

        assertThat(updated).isFalse()
        verify(stringRedisTemplate, never()).opsForHash<String, String>()
    }

    @Test
    fun `getRanking은 점수 오름차순 순서대로 순위를 매기고 recordId로 상세 정보를 조인한다`() {
        val zSetOps: ZSetOperations<String, String> = mock(ZSetOperations::class.java) as ZSetOperations<String, String>
        val hashOps: HashOperations<String, String, String> = mock(HashOperations::class.java) as HashOperations<String, String, String>
        whenever(stringRedisTemplate.opsForZSet()).thenReturn(zSetOps)
        whenever(stringRedisTemplate.opsForHash<String, String>()).thenReturn(hashOps)

        whenever(zSetOps.zCard("ranking:record:1")).thenReturn(2L)
        val tuples: Set<ZSetOperations.TypedTuple<String>> = linkedSetOf(
            DefaultTypedTuple("10", 1500.0),
            DefaultTypedTuple("11", 1620.0)
        )
        whenever(zSetOps.rangeWithScores("ranking:record:1", 0, 1)).thenReturn(tuples)
        whenever(hashOps.multiGet("ranking:record:1:record", listOf("10", "11")))
            .thenReturn(listOf("100", "101"))

        val record100 = buildRecord(100L, buildUser(10L, "런너A"))
        val record101 = buildRecord(101L, buildUser(11L, "런너B"))
        whenever(recordRepository.findByIdIn(listOf(100L, 101L))).thenReturn(listOf(record100, record101))

        val response = service.getRanking(courseId = 1L, limit = 2)

        assertThat(response.totalCount).isEqualTo(2L)
        assertThat(response.entries).hasSize(2)
        assertThat(response.entries[0].rank).isEqualTo(1)
        assertThat(response.entries[0].nickname).isEqualTo("런너A")
        assertThat(response.entries[1].rank).isEqualTo(2)
        assertThat(response.entries[1].nickname).isEqualTo("런너B")
    }

    @Test
    fun `getMyRanking은 0-based rank를 1-based로 변환해서 반환한다`() {
        val zSetOps: ZSetOperations<String, String> = mock(ZSetOperations::class.java) as ZSetOperations<String, String>
        val hashOps: HashOperations<String, String, String> = mock(HashOperations::class.java) as HashOperations<String, String, String>
        whenever(stringRedisTemplate.opsForZSet()).thenReturn(zSetOps)
        whenever(stringRedisTemplate.opsForHash<String, String>()).thenReturn(hashOps)

        whenever(zSetOps.rank("ranking:record:1", "10")).thenReturn(0L)
        whenever(hashOps.get("ranking:record:1:record", "10")).thenReturn("100")

        val record = buildRecord(100L, buildUser(10L, "런너A"))
        whenever(recordRepository.findById(100L)).thenReturn(Optional.of(record))

        val myRanking = service.getMyRanking(courseId = 1L, userId = 10L)

        assertThat(myRanking.hasRecord).isTrue()
        assertThat(myRanking.rank).isEqualTo(1)
        assertThat(myRanking.nickname).isEqualTo("런너A")
    }

    @Test
    fun `랭킹에 없는 유저면 getMyRanking은 hasRecord=false를 반환한다`() {
        val zSetOps: ZSetOperations<String, String> = mock(ZSetOperations::class.java) as ZSetOperations<String, String>
        whenever(stringRedisTemplate.opsForZSet()).thenReturn(zSetOps)
        whenever(zSetOps.rank("ranking:record:1", "99")).thenReturn(null)

        val myRanking = service.getMyRanking(courseId = 1L, userId = 99L)

        assertThat(myRanking.hasRecord).isFalse()
        assertThat(myRanking.rank).isNull()
    }

    @Test
    fun `backfillAll은 publicCourse가 있는 기록만 대상으로 랭킹을 갱신하고 실제 갱신된 개수를 반환한다`() {
        val user1 = buildUser(1L, "런너A")
        val user2 = buildUser(2L, "런너B")
        val recordWithPublicCourse1 = buildRecordWithPublicCourse(id = 100L, owner = user1, publicCourseId = 1L)
        val recordWithPublicCourse2 = buildRecordWithPublicCourse(id = 101L, owner = user2, publicCourseId = 1L)

        whenever(recordRepository.findAllByPublicCourseIsNotNull())
            .thenReturn(listOf(recordWithPublicCourse1, recordWithPublicCourse2))

        val hashOps: HashOperations<String, String, String> = mock(HashOperations::class.java) as HashOperations<String, String, String>
        whenever(stringRedisTemplate.opsForHash<String, String>()).thenReturn(hashOps)
        // 첫 번째 기록만 실제로 더 빠른 기록으로 갱신되고, 두 번째는 갱신되지 않는 상황을 시뮬레이션
        whenever(stringRedisTemplate.execute(any<RedisCallback<Boolean>>()))
            .thenReturn(true)
            .thenReturn(false)

        val updatedCount = service.backfillAll()

        assertThat(updatedCount).isEqualTo(1)
    }

    private fun buildRecordWithPublicCourse(id: Long, owner: RunnectUser, publicCourseId: Long): Record {
        val course = buildCourse(id + 1000, owner)
        val publicCourse = PublicCourse.builder()
            .course(course)
            .title("공개 코스")
            .description("설명")
            .build()
        ReflectionTestUtils.setField(publicCourse, "id", publicCourseId)

        val record = Record.builder()
            .runnectUser(owner)
            .course(course)
            .publicCourse(publicCourse)
            .title("퇴근길 러닝")
            .pace(Time.valueOf("00:05:30"))
            .time(Time.valueOf("00:25:00"))
            .build()
        ReflectionTestUtils.setField(record, "id", id)
        return record
    }

    private fun buildUser(id: Long, nickname: String): RunnectUser {
        val user = RunnectUser.builder()
            .nickname(nickname)
            .socialId("social-$id")
            .email("user$id@runnect.io")
            .provider(SocialType.KAKAO)
            .build()
        ReflectionTestUtils.setField(user, "id", id)
        return user
    }

    private fun buildCourse(id: Long, owner: RunnectUser): Course {
        val course = Course.builder()
            .runnectUser(owner)
            .title("코스 제목")
            .departureRegion("경기")
            .departureCity("시흥시")
            .departureTown("정왕동")
            .departureDetail("정왕본동")
            .departureName("정왕역")
            .distance(5.2f)
            .image("https://image.example/course.png")
            .path(null)
            .build()
        ReflectionTestUtils.setField(course, "id", id)
        return course
    }

    private fun buildRecord(id: Long, owner: RunnectUser): Record {
        val course = buildCourse(id + 1000, owner)
        val record = Record.builder()
            .runnectUser(owner)
            .course(course)
            .title("퇴근길 러닝")
            .pace(Time.valueOf("00:05:30"))
            .time(Time.valueOf("00:25:00"))
            .build()
        ReflectionTestUtils.setField(record, "id", id)
        return record
    }
}
