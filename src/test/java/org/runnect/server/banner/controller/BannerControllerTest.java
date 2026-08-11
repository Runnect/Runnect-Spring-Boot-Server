package org.runnect.server.banner.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.runnect.server.banner.dto.response.BannerResponse;
import org.runnect.server.banner.dto.response.GetBannerResponseDto;
import org.runnect.server.banner.service.BannerService;
import org.runnect.server.config.jwt.JwtService;
import org.runnect.server.config.slack.SlackApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BannerController.class)
class BannerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BannerService bannerService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private SlackApi slackApi;

    @Test
    @DisplayName("GET /api/banner - 정상 요청이면 200과 배너 목록을 반환한다")
    void 정상_조회() throws Exception {
        List<BannerResponse> banners = Collections.singletonList(BannerResponse.of(0, "image.png", "https://a.com"));
        when(bannerService.getBanners()).thenReturn(GetBannerResponseDto.of(banners));

        mockMvc.perform(get("/api/banner"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.banners[0].index").value(0))
            .andExpect(jsonPath("$.data.banners[0].imageUrl").value("image.png"));
    }

    @Test
    @DisplayName("GET /api/banner - 배너가 없으면 200과 빈 목록을 반환한다")
    void 배너_없음() throws Exception {
        when(bannerService.getBanners()).thenReturn(GetBannerResponseDto.of(Collections.emptyList()));

        mockMvc.perform(get("/api/banner"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.banners").isEmpty());
    }
}
