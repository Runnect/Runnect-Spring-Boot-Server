package org.runnect.server.banner.service;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.runnect.server.banner.dto.response.BannerResponse;
import org.runnect.server.banner.dto.response.GetBannerResponseDto;
import org.runnect.server.banner.entity.Banner;
import org.runnect.server.banner.repository.BannerRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerRepository bannerRepository;

    public GetBannerResponseDto getBanners() {
        List<Banner> banners = bannerRepository.findByIsActiveTrueOrderBySortOrderAsc();

        List<BannerResponse> bannerResponses = new ArrayList<>();
        for (int index = 0; index < banners.size(); index++) {
            Banner banner = banners.get(index);
            bannerResponses.add(BannerResponse.of(index, banner.getImageUrl(), banner.getLinkUrl()));
        }

        return GetBannerResponseDto.of(bannerResponses);
    }
}
