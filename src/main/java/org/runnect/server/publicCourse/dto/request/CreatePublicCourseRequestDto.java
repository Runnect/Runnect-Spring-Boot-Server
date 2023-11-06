package org.runnect.server.publicCourse.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CreatePublicCourseRequestDto {
    @NotNull(message = "courseId가 없습니다.")
    Long courseId;

    @NotBlank(message = "제목이 없습니다.")
    String title;

    @NotBlank(message = "설명이 없습니다.")
    String description;
}
