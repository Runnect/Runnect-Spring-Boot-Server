package org.runnect.server.record.service;

import lombok.RequiredArgsConstructor;
import org.runnect.server.course.entity.Course;
import org.runnect.server.course.infrastructure.CourseRepository;
import org.runnect.server.record.dto.request.CreateRecordRequestDto;
import org.runnect.server.record.dto.response.CreateRecordResponseDto;
import org.runnect.server.record.entity.Record;
import org.runnect.server.record.infrastructure.RecordRepository;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.infrastructure.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecordService {

    private final RecordRepository recordRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public CreateRecordResponseDto createRecord(Long userId, CreateRecordRequestDto request) {
        RunnectUser user = userRepository.findById(userId).orElse(null);
        Course course = courseRepository.findById(request.getCourseId());
        Record record = Record.builder()
                .runnectUser(user)
                .course(course)
                .title(request.getTitle())
                .pace(request.getPace())
                .time(request.getTime())
                .build();

        recordRepository.save(record);

        CreateRecordResponseDto response = new CreateRecordResponseDto(record.getId(), record.getCreatedAt());

        return response;

    }


}
