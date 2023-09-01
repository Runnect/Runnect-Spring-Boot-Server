package org.runnect.server.course.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CourseCreateResponseDto {
    private Long id;
    private LocalDateTime createdAt;

    public static CourseCreateResponseDto of(Long id, LocalDateTime createdAt) {
        return new CourseCreateResponseDto(id, createdAt);
    }
}
