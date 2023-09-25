package org.runnect.server.course.dto.request;

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
    @NotNull
    private String path;

    @NotNull
    private String title;

    @NotNull
    private MultipartFile image;

    @NotNull
    private Float distance;

    private String departureName;

    @NotNull
    private String departureAddress;
}
