package org.runnect.server.publicCourse.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdatePublicCourseRequestDto {
    @NotEmpty(message = "수정할 제목이 없습니다.")
    private String title;

    @NotEmpty(message = "수정할 설명이 없습니다.")
    private String description;

}
