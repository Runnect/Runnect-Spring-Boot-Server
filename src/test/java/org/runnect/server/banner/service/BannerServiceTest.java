package org.runnect.server.banner.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.runnect.server.banner.dto.response.GetBannerResponseDto;
import org.runnect.server.banner.entity.Banner;
import org.runnect.server.banner.repository.BannerRepository;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BannerServiceTest {

    @Mock
    private BannerRepository bannerRepository;

    private BannerService bannerService;

    private Banner buildBanner(Long id, String imageUrl, String linkUrl, int sortOrder) {
        Banner banner = Banner.builder().imageUrl(imageUrl).linkUrl(linkUrl).sortOrder(sortOrder).build();
        ReflectionTestUtils.setField(banner, "id", id);
        return banner;
    }

    @Test
    void 활성_배너를_정렬된_순서_그대로_0부터_인덱싱해서_반환한다() {
        bannerService = new BannerService(bannerRepository);
        Banner first = buildBanner(1L, "image1.png", "https://a.com", 0);
        Banner second = buildBanner(2L, "image2.png", "https://b.com", 1);
        when(bannerRepository.findByIsActiveTrueOrderBySortOrderAscIdAsc())
            .thenReturn(Arrays.asList(first, second));

        GetBannerResponseDto response = bannerService.getBanners();

        assertThat(response.getBanners()).hasSize(2);
        assertThat(response.getBanners().get(0).getIndex()).isEqualTo(0);
        assertThat(response.getBanners().get(0).getImageUrl()).isEqualTo("image1.png");
        assertThat(response.getBanners().get(1).getIndex()).isEqualTo(1);
        assertThat(response.getBanners().get(1).getLinkUrl()).isEqualTo("https://b.com");
    }

    @Test
    void 활성_배너가_없으면_빈_목록을_반환한다() {
        bannerService = new BannerService(bannerRepository);
        when(bannerRepository.findByIsActiveTrueOrderBySortOrderAscIdAsc())
            .thenReturn(Collections.emptyList());

        GetBannerResponseDto response = bannerService.getBanners();

        assertThat(response.getBanners()).isEmpty();
    }
}
