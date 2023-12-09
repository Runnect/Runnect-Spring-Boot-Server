package org.runnect.server.course.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CourseCreateRequestDto {

    @NotNull(message = "필수 입력 항목입니다.")
    private CourseRequest data;

    @NotNull(message = "image는 필수 입력 항목입니다.")
    private MultipartFile image;


    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CourseRequest {
        @NotBlank(message = "path는 필수 입력 항목입니다.")
        private String path;

        @NotBlank(message = "title은 필수 입력 항목입니다.")
        private String title;

        @NotNull(message = "image는 필수 입력 항목입니다.")
        private MultipartFile image;

        @NotNull(message = "distance는 필수 입력 항목입니다.")
        private Float distance;

        private String departureName;

        @NotBlank(message = "departureAddress는 필수 입력 항목입니다.")
        private String departureAddress;

    }
}
