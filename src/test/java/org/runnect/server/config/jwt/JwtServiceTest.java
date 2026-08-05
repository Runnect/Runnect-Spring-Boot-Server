package org.runnect.server.config.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.runnect.server.common.constant.TokenStatus;
import org.runnect.server.config.redis.RedisService;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        RedisService redisService = mock(RedisService.class);
        jwtService = new JwtService(redisService);
        ReflectionTestUtils.setField(jwtService, "jwtSecret", "test-secret-key-for-jwt-service-unit-test-only");
        ReflectionTestUtils.invokeMethod(jwtService, "init");
    }

    @Test
    void 발급한_액세스_토큰은_검증에_성공한다() {
        String accessToken = jwtService.issuedAccessToken(1L);

        long status = jwtService.verifyToken(accessToken);

        assertThat(status).isEqualTo(TokenStatus.TOKEN_VALID);
    }

    @Test
    void 발급한_토큰에서_userId_클레임을_그대로_추출한다() {
        String accessToken = jwtService.issuedAccessToken(42L);

        String userId = jwtService.getJwtContents(accessToken);

        assertThat(userId).isEqualTo("42");
    }

    @Test
    void 형식이_깨진_토큰은_INVALID로_판정한다() {
        long status = jwtService.verifyToken("not-a-real-jwt");

        assertThat(status).isEqualTo(TokenStatus.TOKEN_INVALID);
    }

    @Test
    void 이미_만료된_토큰은_EXPIRED로_판정한다() {
        String expiredToken = jwtService.issuedToken("access_token", -1000L, "1");

        long status = jwtService.verifyToken(expiredToken);

        assertThat(status).isEqualTo(TokenStatus.TOKEN_EXPIRED);
    }
}
