package org.runnect.server.record.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.runnect.server.common.exception.NotFoundException;
import org.runnect.server.common.exception.PermissionDeniedException;
import org.runnect.server.course.entity.Course;
import org.runnect.server.course.repository.CourseRepository;
import org.runnect.server.health.entity.RecordHealthData;
import org.runnect.server.health.repository.RecordHealthDataRepository;
import org.runnect.server.publicCourse.entity.PublicCourse;
import org.runnect.server.publicCourse.repository.PublicCourseRepository;
import org.runnect.server.record.dto.request.CreateRecordRequestDto;
import org.runnect.server.record.dto.request.DeleteRecordsRequestDto;
import org.runnect.server.record.dto.request.UpdateRecordRequestDto;
import org.runnect.server.record.dto.response.CreateRecordResponseDto;
import org.runnect.server.record.dto.response.DeleteRecordsResponseDto;
import org.runnect.server.record.dto.response.GetRecordResponseDto;
import org.runnect.server.record.dto.response.RecordResponse;
import org.runnect.server.record.dto.response.UpdateRecordResponseDto;
import org.runnect.server.record.entity.Record;
import org.runnect.server.record.repository.RecordRepository;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.entity.SocialType;
import org.runnect.server.user.entity.StampType;
import org.runnect.server.user.exception.userException.NotFoundUserException;
import org.runnect.server.user.repository.UserRepository;
import org.runnect.server.user.service.UserStampService;
import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@ExtendWith(MockitoExtension.class)
class RecordServiceTest {

    @Mock
    private RecordRepository recordRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private PublicCourseRepository publicCourseRepository;
    @Mock
    private UserStampService userStampService;
    @Mock
    private RecordHealthDataRepository recordHealthDataRepository;
    @Mock
    private org.runnect.server.ranking.service.RecordRankingService recordRankingService;

    private RecordService recordService;

    @BeforeEach
    void setUp() {
        recordService = new RecordService(recordRepository, userRepository, courseRepository,
            publicCourseRepository, userStampService, recordHealthDataRepository, recordRankingService);
    }

    private RunnectUser buildUser(Long id) {
        RunnectUser user = RunnectUser.builder()
            .nickname("러너" + id)
            .socialId("social-" + id)
            .email("user" + id + "@runnect.io")
            .provider(SocialType.KAKAO)
            .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Course buildCourse(Long id, RunnectUser owner) {
        Course course = Course.builder()
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
            .build();
        ReflectionTestUtils.setField(course, "id", id);
        return course;
    }

    private Record buildRecord(Long id, RunnectUser owner, Course course, PublicCourse publicCourse) {
        Record record = Record.builder()
            .runnectUser(owner)
            .course(course)
            .publicCourse(publicCourse)
            .title("퇴근길 러닝")
            .pace(java.sql.Time.valueOf("00:05:30"))
            .time(java.sql.Time.valueOf("00:25:00"))
            .build();
        ReflectionTestUtils.setField(record, "id", id);
        ReflectionTestUtils.setField(record, "createdAt", LocalDateTime.of(2026, 1, 1, 0, 0));
        return record;
    }

    private CreateRecordRequestDto createRecordRequestDto(Long courseId, Long publicCourseId, String time,
        String pace) {
        CreateRecordRequestDto dto = BeanUtils.instantiateClass(CreateRecordRequestDto.class);
        ReflectionTestUtils.setField(dto, "courseId", courseId);
        ReflectionTestUtils.setField(dto, "publicCourseId", publicCourseId);
        ReflectionTestUtils.setField(dto, "title", "퇴근길 러닝");
        ReflectionTestUtils.setField(dto, "time", time);
        ReflectionTestUtils.setField(dto, "pace", pace);
        return dto;
    }

    private UpdateRecordRequestDto updateRecordRequestDto(String title) {
        UpdateRecordRequestDto dto = BeanUtils.instantiateClass(UpdateRecordRequestDto.class);
        ReflectionTestUtils.setField(dto, "title", title);
        return dto;
    }

    private void stubSaveSetsCreatedAt() {
        doAnswer(invocation -> {
            Record record = invocation.getArgument(0);
            ReflectionTestUtils.setField(record, "id", 100L);
            ReflectionTestUtils.setField(record, "createdAt", LocalDateTime.of(2026, 1, 1, 0, 0));
            return null;
        }).when(recordRepository).save(any(Record.class));
    }

    @Nested
    @DisplayName("createRecord")
    class CreateRecord {

        @Test
        @DisplayName("courseId로 직접 생성하면 코스를 조회해서 기록을 저장한다")
        void 코스아이디로_정상_생성() {
            RunnectUser user = buildUser(1L);
            Course course = buildCourse(10L, user);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
            stubSaveSetsCreatedAt();

            CreateRecordRequestDto request = createRecordRequestDto(10L, null, "00:25:00", "00:05:30");

            CreateRecordResponseDto response = recordService.createRecord(1L, request);

            assertThat(response.getRecord().getId()).isEqualTo(100L);
            assertThat(user.getCreatedRecord()).isEqualTo(1L);
            verify(userStampService).createStampByUser(user, StampType.r);
            verify(publicCourseRepository, never()).findById(any());
            verify(recordRankingService, never()).updateBestRecord(anyLong(), anyLong(), anyLong(), any());
        }

        @Test
        @DisplayName("publicCourseId로 생성하면 공개 코스를 통해 코스를 가져오고 courseRepository는 조회하지 않는다")
        void 퍼블릭코스아이디로_정상_생성() {
            RunnectUser user = buildUser(1L);
            Course course = buildCourse(10L, user);
            PublicCourse publicCourse = PublicCourse.builder()
                .course(course)
                .title("공개 코스")
                .description("설명")
                .build();
            ReflectionTestUtils.setField(publicCourse, "id", 20L);

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(publicCourseRepository.findById(20L)).thenReturn(Optional.of(publicCourse));
            stubSaveSetsCreatedAt();

            CreateRecordRequestDto request = createRecordRequestDto(null, 20L, "00:25:00", "00:05:30");

            recordService.createRecord(1L, request);

            verify(courseRepository, never()).findById(any());
            verify(recordRankingService).updateBestRecord(20L, 1L, 100L, java.sql.Time.valueOf("00:25:00"));
        }

        @Test
        @DisplayName("트랜잭션이 활성화된 상태면 랭킹 갱신은 커밋 이후로 지연되고, 커밋 전에는 호출되지 않는다")
        void 트랜잭션_커밋_이후에_랭킹이_갱신된다() {
            RunnectUser user = buildUser(1L);
            Course course = buildCourse(10L, user);
            PublicCourse publicCourse = PublicCourse.builder()
                .course(course)
                .title("공개 코스")
                .description("설명")
                .build();
            ReflectionTestUtils.setField(publicCourse, "id", 20L);

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(publicCourseRepository.findById(20L)).thenReturn(Optional.of(publicCourse));
            stubSaveSetsCreatedAt();

            CreateRecordRequestDto request = createRecordRequestDto(null, 20L, "00:25:00", "00:05:30");

            TransactionSynchronizationManager.initSynchronization();
            try {
                recordService.createRecord(1L, request);

                verify(recordRankingService, never()).updateBestRecord(anyLong(), anyLong(), anyLong(), any());

                TransactionSynchronizationManager.getSynchronizations()
                    .forEach(TransactionSynchronization::afterCommit);
            } finally {
                TransactionSynchronizationManager.clearSynchronization();
            }

            verify(recordRankingService).updateBestRecord(20L, 1L, 100L, java.sql.Time.valueOf("00:25:00"));
        }

        @Test
        @DisplayName("존재하지 않는 코스면 NotFoundException")
        void 존재하지_않는_코스() {
            when(courseRepository.findById(10L)).thenReturn(Optional.empty());

            CreateRecordRequestDto request = createRecordRequestDto(10L, null, "00:25:00", "00:05:30");

            assertThatThrownBy(() -> recordService.createRecord(1L, request))
                .isInstanceOf(NotFoundException.class);

            verify(recordRepository, never()).save(any());
        }

        @Test
        @DisplayName("존재하지 않는 공개 코스면 NotFoundException")
        void 존재하지_않는_공개_코스() {
            when(publicCourseRepository.findById(20L)).thenReturn(Optional.empty());

            CreateRecordRequestDto request = createRecordRequestDto(null, 20L, "00:25:00", "00:05:30");

            assertThatThrownBy(() -> recordService.createRecord(1L, request))
                .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("존재하지 않는 유저면 NotFoundUserException")
        void 존재하지_않는_유저() {
            Course course = buildCourse(10L, buildUser(2L));
            when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            CreateRecordRequestDto request = createRecordRequestDto(10L, null, "00:25:00", "00:05:30");

            assertThatThrownBy(() -> recordService.createRecord(1L, request))
                .isInstanceOf(NotFoundUserException.class);
        }

        @Test
        @DisplayName("시간 형식이 잘못되면 IllegalArgumentException (컨트롤러 어드바이스가 400으로 매핑)")
        void 시간_형식이_잘못됨() {
            RunnectUser user = buildUser(1L);
            Course course = buildCourse(10L, user);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(courseRepository.findById(10L)).thenReturn(Optional.of(course));

            CreateRecordRequestDto request = createRecordRequestDto(10L, null, "잘못된시간", "00:05:30");

            assertThatThrownBy(() -> recordService.createRecord(1L, request))
                .isInstanceOf(IllegalArgumentException.class);

            verify(recordRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getRecordByUser")
    class GetRecordByUser {

        @Test
        @DisplayName("유저의 기록 목록을 매핑해서 반환한다")
        void 정상_조회() {
            RunnectUser user = buildUser(1L);
            Course course = buildCourse(10L, user);
            Record record = buildRecord(50L, user, course, null);
            when(recordRepository.findAllByUserId(1L)).thenReturn(Collections.singletonList(record));
            when(recordHealthDataRepository.findByRecordId(50L)).thenReturn(Optional.empty());

            GetRecordResponseDto response = recordService.getRecordByUser(1L);

            assertThat(response.getUser().getUserId()).isEqualTo(1L);
            assertThat(response.getRecords()).hasSize(1);
            RecordResponse recordResponse = response.getRecords().get(0);
            assertThat(recordResponse.getId()).isEqualTo(50L);
            assertThat(recordResponse.getCourseId()).isEqualTo(10L);
            assertThat(recordResponse.getPublicCourseId()).isNull();
            assertThat(recordResponse.getUserId()).isEqualTo(1L);
            assertThat(recordResponse.getDeparture().getRegion()).isEqualTo("경기");
            assertThat(recordResponse.getHealthData()).isNull();
        }

        @Test
        @DisplayName("공개 코스로 만든 기록이면 publicCourseId가 채워진다")
        void 퍼블릭코스_아이디_매핑() {
            RunnectUser user = buildUser(1L);
            Course course = buildCourse(10L, user);
            PublicCourse publicCourse = PublicCourse.builder().course(course).title("t").description("d").build();
            ReflectionTestUtils.setField(publicCourse, "id", 20L);
            Record record = buildRecord(50L, user, course, publicCourse);
            when(recordRepository.findAllByUserId(1L)).thenReturn(Collections.singletonList(record));
            when(recordHealthDataRepository.findByRecordId(50L)).thenReturn(Optional.empty());

            GetRecordResponseDto response = recordService.getRecordByUser(1L);

            assertThat(response.getRecords().get(0).getPublicCourseId()).isEqualTo(20L);
        }

        @Test
        @DisplayName("건강 데이터가 있으면 함께 반환한다")
        void 건강_데이터_포함() {
            RunnectUser user = buildUser(1L);
            Course course = buildCourse(10L, user);
            Record record = buildRecord(50L, user, course, null);
            RecordHealthData healthData = mock(RecordHealthData.class);
            when(healthData.getAvgHeartRate()).thenReturn(145.5);
            when(healthData.getCalories()).thenReturn(320.0);
            when(recordRepository.findAllByUserId(1L)).thenReturn(Collections.singletonList(record));
            when(recordHealthDataRepository.findByRecordId(50L)).thenReturn(Optional.of(healthData));

            GetRecordResponseDto response = recordService.getRecordByUser(1L);

            assertThat(response.getRecords().get(0).getHealthData().getAvgHeartRate()).isEqualTo(145.5);
            assertThat(response.getRecords().get(0).getHealthData().getCalories()).isEqualTo(320.0);
        }

        @Test
        @DisplayName("건강 데이터 조회가 실패해도 기록 목록 조회 자체는 실패하지 않는다")
        void 건강_데이터_조회_실패해도_정상_반환() {
            RunnectUser user = buildUser(1L);
            Course course = buildCourse(10L, user);
            Record record = buildRecord(50L, user, course, null);
            when(recordRepository.findAllByUserId(1L)).thenReturn(Collections.singletonList(record));
            when(recordHealthDataRepository.findByRecordId(50L)).thenThrow(new RuntimeException("DB 오류"));

            GetRecordResponseDto response = recordService.getRecordByUser(1L);

            assertThat(response.getRecords()).hasSize(1);
            assertThat(response.getRecords().get(0).getHealthData()).isNull();
        }

        @Test
        @DisplayName("기록이 없으면 빈 목록을 반환한다")
        void 기록이_없으면_빈_목록() {
            when(recordRepository.findAllByUserId(1L)).thenReturn(Collections.emptyList());

            GetRecordResponseDto response = recordService.getRecordByUser(1L);

            assertThat(response.getRecords()).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateRecord")
    class UpdateRecord {

        @Test
        @DisplayName("본인 기록이면 제목을 수정한다")
        void 정상_수정() {
            RunnectUser owner = buildUser(1L);
            Record record = buildRecord(50L, owner, buildCourse(10L, owner), null);
            when(recordRepository.findById(50L)).thenReturn(Optional.of(record));

            UpdateRecordResponseDto response = recordService.updateRecord(1L, 50L, updateRecordRequestDto("새 제목"));

            assertThat(record.getTitle()).isEqualTo("새 제목");
            assertThat(response.getRecord().getTitle()).isEqualTo("새 제목");
        }

        @Test
        @DisplayName("존재하지 않는 기록이면 NotFoundException")
        void 존재하지_않는_기록() {
            when(recordRepository.findById(50L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> recordService.updateRecord(1L, 50L, updateRecordRequestDto("새 제목")))
                .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("본인 기록이 아니면 PermissionDeniedException (IDOR 방지)")
        void 소유자가_아니면_수정_불가() {
            RunnectUser owner = buildUser(1L);
            Record record = buildRecord(50L, owner, buildCourse(10L, owner), null);
            when(recordRepository.findById(50L)).thenReturn(Optional.of(record));

            Long 다른유저Id = 999L;

            assertThatThrownBy(() -> recordService.updateRecord(다른유저Id, 50L, updateRecordRequestDto("남의 기록 수정")))
                .isInstanceOf(PermissionDeniedException.class);

            assertThat(record.getTitle()).isEqualTo("퇴근길 러닝");
        }
    }

    @Nested
    @DisplayName("deleteRecords")
    class DeleteRecords {

        @Test
        @DisplayName("본인 기록만 있으면 정상 삭제한다")
        void 정상_삭제() {
            RunnectUser user = buildUser(1L);
            Record record1 = buildRecord(50L, user, buildCourse(10L, user), null);
            Record record2 = buildRecord(51L, user, buildCourse(10L, user), null);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(recordRepository.findByIdIn(Arrays.asList(50L, 51L))).thenReturn(Arrays.asList(record1, record2));
            when(recordRepository.deleteByIdIn(Arrays.asList(50L, 51L))).thenReturn(2L);

            DeleteRecordsResponseDto response = recordService.deleteRecords(1L,
                new DeleteRecordsRequestDto(Arrays.asList(50L, 51L)));

            assertThat(response.getDeletedRecordIdCount()).isEqualTo(2L);
        }

        @Test
        @DisplayName("요청한 id 중 존재하지 않는 게 있으면 NotFoundException")
        void 존재하지_않는_기록_포함() {
            RunnectUser user = buildUser(1L);
            Record record1 = buildRecord(50L, user, buildCourse(10L, user), null);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(recordRepository.findByIdIn(Arrays.asList(50L, 999L))).thenReturn(
                Collections.singletonList(record1));

            assertThatThrownBy(() -> recordService.deleteRecords(1L,
                new DeleteRecordsRequestDto(Arrays.asList(50L, 999L))))
                .isInstanceOf(NotFoundException.class);

            verify(recordRepository, never()).deleteByIdIn(any());
        }

        @Test
        @DisplayName("본인 소유가 아닌 기록이 섞여 있으면 PermissionDeniedException")
        void 소유자가_아닌_기록_포함() {
            RunnectUser user = buildUser(1L);
            RunnectUser otherUser = buildUser(2L);
            Record ownRecord = buildRecord(50L, user, buildCourse(10L, user), null);
            Record othersRecord = buildRecord(51L, otherUser, buildCourse(11L, otherUser), null);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(recordRepository.findByIdIn(Arrays.asList(50L, 51L))).thenReturn(
                Arrays.asList(ownRecord, othersRecord));

            assertThatThrownBy(() -> recordService.deleteRecords(1L,
                new DeleteRecordsRequestDto(Arrays.asList(50L, 51L))))
                .isInstanceOf(PermissionDeniedException.class);

            verify(recordRepository, never()).deleteByIdIn(any());
        }

        @Test
        @DisplayName("존재하지 않는 유저면 NotFoundUserException")
        void 존재하지_않는_유저() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> recordService.deleteRecords(1L,
                new DeleteRecordsRequestDto(Collections.singletonList(50L))))
                .isInstanceOf(NotFoundUserException.class);
        }
    }
}
