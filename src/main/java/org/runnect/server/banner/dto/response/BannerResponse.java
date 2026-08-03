package org.runnect.server.banner.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BannerResponse {
    private Integer index;
    private String imageUrl;
    private String linkUrl;

    public static BannerResponse of(Integer index, String imageUrl, String linkUrl) {
        return new BannerResponse(index, imageUrl, linkUrl);
    }
}
