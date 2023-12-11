package org.runnect.server.course.dto.request;

import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.runnect.server.common.module.convert.CoordinateDto;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CourseCreateRequestDtoV2 implements Serializable {

    @NotNull(message = "path는 필수 입력 항목입니다.")
    private List<CoordinateDto> path;

    @NotBlank(message = "title은 필수 입력 항목입니다.")
    private String title;

    @NotNull(message = "distance는 필수 입력 항목입니다.")
    private Float distance;

    private String departureName;

    @NotBlank(message = "departureAddress는 필수 입력 항목입니다.")
    private String departureAddress;
}
