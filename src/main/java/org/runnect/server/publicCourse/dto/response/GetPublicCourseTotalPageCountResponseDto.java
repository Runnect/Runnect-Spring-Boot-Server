package org.runnect.server.publicCourse.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GetPublicCourseTotalPageCountResponseDto {
    private Long totalPageCount;

    public static GetPublicCourseTotalPageCountResponseDto of(Long totalPageCount){
        return new GetPublicCourseTotalPageCountResponseDto(totalPageCount);
    }
}
