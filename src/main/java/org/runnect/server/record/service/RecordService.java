package org.runnect.server.record.service;

import java.sql.Time;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.runnect.server.common.constant.ErrorStatus;
import org.runnect.server.common.exception.NotFoundException;
import org.runnect.server.common.exception.PermissionDeniedException;
import org.runnect.server.course.entity.Course;
import org.runnect.server.course.repository.CourseRepository;
import org.runnect.server.publicCourse.entity.PublicCourse;
import org.runnect.server.publicCourse.repository.PublicCourseRepository;
import org.runnect.server.record.dto.request.CreateRecordRequestDto;
import org.runnect.server.record.dto.request.DeleteRecordsRequestDto;
import org.runnect.server.record.dto.request.UpdateRecordRequestDto;
import org.runnect.server.record.dto.response.CreateRecordDto;
import org.runnect.server.record.dto.response.CreateRecordResponseDto;
import org.runnect.server.record.dto.response.DeleteRecordsResponseDto;
import org.runnect.server.health.entity.RecordHealthData;
import org.runnect.server.health.repository.RecordHealthDataRepository;
import org.runnect.server.record.dto.response.DepartureResponse;
import org.runnect.server.record.dto.response.GetRecordResponseDto;
import org.runnect.server.record.dto.response.HealthDataResponse;
import org.runnect.server.record.dto.response.RecordResponse;
import org.runnect.server.record.dto.response.UpdateRecordResponse;
import org.runnect.server.record.dto.response.UpdateRecordResponseDto;
import org.runnect.server.record.dto.response.UserResponse;
import org.runnect.server.record.entity.Record;
import org.runnect.server.record.repository.RecordRepository;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.entity.StampType;
import org.runnect.server.user.exception.userException.NotFoundUserException;
import org.runnect.server.user.repository.UserRepository;
import org.runnect.server.user.service.UserStampService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecordService {

    private final RecordRepository recordRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final PublicCourseRepository publicCourseRepository;
    private final UserStampService userStampService;
    private final RecordHealthDataRepository recordHealthDataRepository;

    @Transactional
    public CreateRecordResponseDto createRecord(Long userId, CreateRecordRequestDto request) {

        //publicCourseId가 request로 들어왔을 때
        PublicCourse publicCourse = null;
        Course course = null;
        if (request.getPublicCourseId() != null) {
            // fetch join으로 course 정보 가져옴
            publicCourse = publicCourseRepository.findById(request.getPublicCourseId())
                    .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_COURSE_EXCEPTION, ErrorStatus.NOT_FOUND_COURSE_EXCEPTION.getMessage()));
            course = publicCourse.getCourse();
        } else {
            // public course id가 들어오지 않으면 fetch join을 사용할 수 없으므로 따로 쿼리로 조회
            course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_COURSE_EXCEPTION, ErrorStatus.NOT_FOUND_COURSE_EXCEPTION.getMessage()));
        }

        RunnectUser user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundUserException(ErrorStatus.NOT_FOUND_USER_EXCEPTION, ErrorStatus.NOT_FOUND_USER_EXCEPTION.getMessage()));

        Time pace = Time.valueOf(request.getPace());
        Time time = Time.valueOf(request.getTime());

        Record record = Record.builder()
                .runnectUser(user)
                .course(course)
                .publicCourse(publicCourse)
                .title(request.getTitle())
                .pace(pace)
                .time(time)
                .build();

        recordRepository.save(record);

        user.updateCreatedRecord();
        userStampService.createStampByUser(user, StampType.r);

        CreateRecordDto recordDto = new CreateRecordDto(record.getId(), record.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));

        CreateRecordResponseDto response = new CreateRecordResponseDto(recordDto);

        return response;

    }

    public GetRecordResponseDto getRecordByUser(Long userId) {
        UserResponse user = UserResponse.of(userId);

        List<Record> records = recordRepository.findAllByUserId(userId);
        List<RecordResponse> recordResponses  = new ArrayList<RecordResponse>();

        for (Record record: records) {
            Course course = record.getCourse();

            // public course id가 없는 record일 때
            Long publicCourseId = null;
            if (record.getPublicCourse() != null) {
                publicCourseId = record.getPublicCourse().getId();
            }

            DepartureResponse departure = DepartureResponse.of(course.getDepartureRegion(), course.getDepartureCity());

            // 건강 데이터 조회
            HealthDataResponse healthData = recordHealthDataRepository.findByRecordId(record.getId())
                    .map(h -> HealthDataResponse.of(h.getAvgHeartRate(), h.getCalories()))
                    .orElse(null);

            RecordResponse recordResponse = RecordResponse.of(record.getId(), course.getId(), publicCourseId, userId,
                    record.getTitle(), course.getImage(), record.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), course.getDistance(), record.getTime().toString(),
                    record.getPace().toString(), departure, healthData);

            recordResponses.add(recordResponse);

        }

        return GetRecordResponseDto.of(user, recordResponses);

    }

    @Transactional
    public UpdateRecordResponseDto updateRecord(Long userId, Long recordId, UpdateRecordRequestDto request) {
        Record record = recordRepository.findById(recordId)
                .orElseThrow(()->new NotFoundException(ErrorStatus.NOT_FOUND_RECORD_EXCEPTION, ErrorStatus.NOT_FOUND_RECORD_EXCEPTION.getMessage()));

        record.updateRecord(request.getTitle());

        return UpdateRecordResponseDto.of(UpdateRecordResponse.of(record.getId(), request.getTitle()));
    }

    @Transactional
    public DeleteRecordsResponseDto deleteRecords(
        Long userId,
        DeleteRecordsRequestDto requestDto
    ) {
        RunnectUser user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundUserException(ErrorStatus.NOT_FOUND_USER_EXCEPTION,
                ErrorStatus.NOT_FOUND_USER_EXCEPTION.getMessage()));

        List<Record> records = recordRepository.findByIdIn(requestDto.getRecordIdList());

        if (records.size() != requestDto.getRecordIdList().size()) {
            throw new NotFoundException(ErrorStatus.NOT_FOUND_RECORD_EXCEPTION, ErrorStatus.NOT_FOUND_RECORD_EXCEPTION.getMessage());
        }

        records.stream()
            .filter(record -> !record.getRunnectUser().equals(user))
            .findAny()
            .ifPresent(record -> {
                throw new PermissionDeniedException(
                    ErrorStatus.PERMISSION_DENIED_RECORD_DELETE_EXCEPTION,
                    ErrorStatus.PERMISSION_DENIED_RECORD_DELETE_EXCEPTION.getMessage()
                );
            });

        Long deletedCount = recordRepository.deleteByIdIn(requestDto.getRecordIdList());
        return DeleteRecordsResponseDto.from(deletedCount);
    }

}