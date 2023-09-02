package org.runnect.server.record.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.time.LocalTime;
import java.util.Optional;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateRecordRequestDto {
    private Long courseId;
    private Long publicCourseId;
    @NotEmpty(message = "경로 타이틀 없음")
    private String title;
    @NotEmpty(message = "경로 뛴 시간 없음")
    private String time;
    @NotEmpty(message = "경로 뛴 페이스 없음")
    private String pace;
}
