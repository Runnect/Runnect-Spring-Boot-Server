package org.runnect.server.course.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class CourseCreateRequestDto {
    @NotNull
    private String path;

    @NotNull
    private MultipartFile image;

    @NotNull
    private Float distance;

    private String departureName;

    @NotNull
    private String departureAddress;
}
