package org.runnect.server.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.runnect.server.common.exception.BasicException;
import org.runnect.server.common.exception.ErrorStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final long accessTokenExpiryTime = 1000L * 60 * 60 * 2; // 2시간
    private final long refreshTokenExpiryTime = 1000L * 60 * 60 * 24 * 14; // 2주
    private final String CLAIM_NAME = "userId";

    private final RedisTemplate<String, String> redisTemplate;

    @PostConstruct
    protected void init() {
        jwtSecret = Base64.getEncoder()
                .encodeToString(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // Access Token 발급
    public String issuedAccessToken(Long userId) {
        return issuedToken("access_token", accessTokenExpiryTime, userId.toString());
    }

    // Refresh Token 발급
    public String issuedRefreshToken(Long userId) {
        String refreshToken = issuedToken("refresh_token", refreshTokenExpiryTime, userId.toString());
        redisTemplate.opsForValue().set(String.valueOf(userId), refreshToken, Duration.ofMillis(refreshTokenExpiryTime));
        return refreshToken;
    }

    // JWT 토큰 발급
    public String issuedToken(String tokenName, long expiryTime, String userId) {
        final Date now = new Date();

        final Claims claims = Jwts.claims()
                .setSubject(tokenName)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiryTime));

        claims.put(CLAIM_NAME, userId);

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setClaims(claims)
                .signWith(getSigningKey())
                .compact();
    }

    private Key getSigningKey() {
        final byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // JWT 토큰 검증
    public boolean verifyToken(String token) {
        try {
            final Claims claims = getBody(token);
            return true;
        } catch (RuntimeException e) {
            if (e instanceof ExpiredJwtException) {
                throw new BasicException(ErrorStatus.TOKEN_TIME_EXPIRED_EXCEPTION, ErrorStatus.TOKEN_TIME_EXPIRED_EXCEPTION.getMessage());
            }
            return false;
        }
    }

    private Claims getBody(final String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // JWT 토큰 내용 확인
    public String getJwtContents(String token) {
        final Claims claims = getBody(token);
        return (String) claims.get(CLAIM_NAME);
    }

}
