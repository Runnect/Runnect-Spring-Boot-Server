package org.runnect.server.scrap.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateAndDeleteScrapDto {
    private Long scrapCount;

    public static CreateAndDeleteScrapDto of (Long scrapCount) {
        return new CreateAndDeleteScrapDto(scrapCount);
    }
}
