package org.runnect.server.scrap.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateAndDeleteScrapResponseDto {
    private Long scrapCount;

    public static CreateAndDeleteScrapResponseDto of (Long scrapCount) {
        return new CreateAndDeleteScrapResponseDto(scrapCount);
    }
}
