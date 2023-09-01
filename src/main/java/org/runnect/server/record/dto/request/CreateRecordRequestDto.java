package org.runnect.server.record.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.Optional;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateRecordRequestDto {
    private Long courseId;
    private Optional<Integer> publicCourseId;
    private String title;
    private String time;
    private String pace;
}
