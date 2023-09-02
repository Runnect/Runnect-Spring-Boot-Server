package org.runnect.server.record.service;

import lombok.RequiredArgsConstructor;
import org.runnect.server.common.exception.BasicException;
import org.runnect.server.common.exception.ErrorStatus;
import org.runnect.server.common.exception.NotFoundException;
import org.runnect.server.course.entity.Course;
import org.runnect.server.course.repository.CourseRepository;
import org.runnect.server.publicCourse.entity.PublicCourse;
import org.runnect.server.publicCourse.repository.PublicCourseRepository;
import org.runnect.server.record.dto.request.CreateRecordRequestDto;
import org.runnect.server.record.dto.response.CreateRecordDto;
import org.runnect.server.record.dto.response.CreateRecordResponseDto;
import org.runnect.server.record.entity.Record;
import org.runnect.server.record.repository.RecordRepository;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.exception.userException.NotFoundUserException;
import org.runnect.server.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class RecordService {

    private final RecordRepository recordRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final PublicCourseRepository publicCourseRepository;

    @Transactional
    public CreateRecordResponseDto createRecord(Long userId, CreateRecordRequestDto request) {
//        if (request.getTitle() == null) {
//            throw new BasicException(ErrorStatus.NO_RECORD_TITLE, ErrorStatus.NO_RECORD_TITLE.getMessage());
//        }
//        if (request.getTime() == null) {
//            throw new BasicException(ErrorStatus.NO_RECORD_TIME, ErrorStatus.NO_RECORD_TIME.getMessage());
//        }
//        if (request.getPace() == null) {
//            throw new BasicException(ErrorStatus.NO_RECORD_PACE, ErrorStatus.NO_RECORD_PACE.getMessage());
//        }

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

        CreateRecordDto recordDto = new CreateRecordDto(record.getId(), record.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));

        CreateRecordResponseDto response = new CreateRecordResponseDto(recordDto);

        return response;

    }


}