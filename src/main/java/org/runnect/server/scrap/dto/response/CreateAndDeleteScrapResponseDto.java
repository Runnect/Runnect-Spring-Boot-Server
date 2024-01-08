package org.runnect.server.scrap.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateAndDeleteScrapResponseDto {

    private Long publicCourseId;
    private Long scrapCount;
    private Boolean scrapTF;

    public static CreateAndDeleteScrapResponseDto of (Long publicCourseId, Long scrapCount, Boolean scrapTF) {
        return new CreateAndDeleteScrapResponseDto(publicCourseId, scrapCount, scrapTF);
    }
}
