package org.runnect.server.scrap.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GetScrapCourseResponseDto {
    UserResponse user;

    // 명세서상 response에서 Scraps로 앞 문자가 대문자라서 @JsonProperty로 리턴시 이름 지정
    @JsonProperty("Scraps")
    List<ScrapResponse> Scraps;

    public static GetScrapCourseResponseDto of (UserResponse user, List<ScrapResponse> Scraps) {
        return new GetScrapCourseResponseDto(user, Scraps);
    }
}
