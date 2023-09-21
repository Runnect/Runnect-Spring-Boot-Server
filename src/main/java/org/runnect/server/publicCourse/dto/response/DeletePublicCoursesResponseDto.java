package org.runnect.server.publicCourse.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DeletePublicCoursesResponseDto {

    private int deletedPublicCourseCount;

    public static DeletePublicCoursesResponseDto from(final int deletedPublicCourseCount) {
        return new DeletePublicCoursesResponseDto(deletedPublicCourseCount);
    }
}
