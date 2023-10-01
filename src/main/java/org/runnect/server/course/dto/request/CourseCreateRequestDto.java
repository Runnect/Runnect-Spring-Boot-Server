package org.runnect.server.course.dto.request;

import javax.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CourseCreateRequestDto {
    @NotBlank(message = "path는 필수 입력 항목입니다.")
    private String path;

    @NotBlank(message = "title은 필수 입력 항목입니다.")
    private String title;

    @NotBlank(message = "image는 필수 입력 항목입니다.")
    private MultipartFile image;

    @NotBlank(message = "distance는 필수 입력 항목입니다.")
    private Float distance;

    private String departureName;

    @NotBlank(message = "departureAddress는 필수 입력 항목입니다.")
    private String departureAddress;
}
