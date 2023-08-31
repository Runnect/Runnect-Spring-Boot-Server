package org.runnect.server.record.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RecordRequestDto {
    private Integer courseId;
    private Optional<Integer> publicCourseId;
    private String title;
    private String time;
    private String pace;
}
