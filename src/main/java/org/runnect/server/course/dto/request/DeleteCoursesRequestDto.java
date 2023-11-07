package org.runnect.server.course.dto.request;

import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DeleteCoursesRequestDto {

    @NotNull(message = "courseIdList는 필수 입력 항목입니다.")
    private List<Long> courseIdList;
}
