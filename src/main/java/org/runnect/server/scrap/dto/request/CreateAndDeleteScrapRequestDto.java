package org.runnect.server.scrap.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateAndDeleteScrapRequestDto {
    @NotNull(message = "퍼블릭 코스 아이디가 없음")
    private Long publicCourseId;

    @NotNull(message = "스크랩 여부가 없음")
    private Boolean scrapTF;
}
