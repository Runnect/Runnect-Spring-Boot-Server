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

    List<ScrapResponse> scraps;

    public static GetScrapCourseResponseDto of (UserResponse user, List<ScrapResponse> Scraps) {
        return new GetScrapCourseResponseDto(user, Scraps);
    }
}
