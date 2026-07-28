package org.runnect.server.auth.service;


import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.runnect.server.auth.dto.response.SocialInfoResponseDto;
import org.runnect.server.common.constant.ErrorStatus;
import org.runnect.server.common.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;


@Service
@RequiredArgsConstructor
public class AppleSignInService {
    @Value("${apple.key-id}")
    private String APPLE_KEY_ID;

    @Value("${apple.team-id}")
    private String APPLE_TEAM_ID;

    @Value("${apple.bundle-id}")
    private String APPLE_BUNDLE_ID;

    @Value("${apple.issue-url}")
    private String APPLE_ISSUE_URL;

    @Value("${apple.revoke-url}")
    private String APPLE_REVOKE_URL;
    private static final String APPLE_JWKS_URL = "https://appleid.apple.com/auth/keys";

    private PrivateKey PRIVATE_KEY;
    @Value("${apple.p8key}")
    private void getPrivateKey(String P8KEY){
        try{
            byte[] privateKeyBytes = java.util.Base64.getDecoder().decode(P8KEY);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PRIVATE_KEY =  keyFactory.generatePrivate(keySpec);
        }catch (Exception e){
            throw new UnauthorizedException(ErrorStatus.INVALID_APPLE_P8KEY_EXCEPTION,
                    ErrorStatus.INVALID_APPLE_P8KEY_EXCEPTION.getMessage());
        }
    }

    public SocialInfoResponseDto getSocialInfo(String idToken) {

        // 클라에서 준 인증토큰이 정말 애플에서 발급받은게 맞는지 확인
        // (애플 공개키(JWKS)로 서명을 검증해야만 위조된 토큰을 걸러낼 수 있다 —
        // 서명 검증 없이 파싱만 하면 누구나 클레임을 임의로 채운 토큰으로 로그인할 수 있음)

        try{
            JWTClaimsSet claimsSet = verifySignatureAndGetClaims(idToken);

            // 발급처, aud, 시간제한, 이메일 검증

            //만료된경우
            if (System.currentTimeMillis() > claimsSet.getExpirationTime().getTime()) {
                throw new UnauthorizedException(ErrorStatus.APPLE_ID_TOKEN_TIME_EXPIRED_EXCEPTION,
                        ErrorStatus.APPLE_ID_TOKEN_TIME_EXPIRED_EXCEPTION.getMessage());
            }



            Boolean emailVerified = (Boolean) claimsSet.getClaim("email_verified");
            if (!claimsSet.getIssuer().equals(APPLE_ISSUE_URL) ||
                    !claimsSet.getAudience().get(0).equals(APPLE_BUNDLE_ID) ||
                    emailVerified == null || !emailVerified) {
                throw new UnauthorizedException(ErrorStatus.INVALID_APPLE_ID_TOKEN_EXCEPTION,
                        ErrorStatus.INVALID_APPLE_ID_TOKEN_EXCEPTION.getMessage());

            }

            return SocialInfoResponseDto.of(claimsSet.getStringClaim("email"), claimsSet.getSubject());

        }catch (UnauthorizedException e){
            throw e;
        }catch (Exception e){
            // 서명 검증 실패(BadJOSEException), 파싱 실패(ParseException), JWKS 조회 실패(JOSEException) 등
            // 위조/변조된 토큰 또는 애플 검증 자체가 불가능한 경우 전부 동일하게 처리
            throw new UnauthorizedException(ErrorStatus.INVALID_APPLE_ID_TOKEN_EXCEPTION,
                    ErrorStatus.INVALID_APPLE_ID_TOKEN_EXCEPTION.getMessage());
        }

    }

    private JWTClaimsSet verifySignatureAndGetClaims(String idToken) throws Exception {
        JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(new URL(APPLE_JWKS_URL));
        ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        JWSVerificationKeySelector<SecurityContext> keySelector =
                new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, keySource);
        jwtProcessor.setJWSKeySelector(keySelector);
        // 서명이 유효하지 않으면 여기서 BadJOSEException/JOSEException이 던져진다
        return jwtProcessor.process(idToken, null);
    }

//       id_token 형태 :
//       {
//       "aud": 번들아이디,
//       "sub": 애플 유니크 소셜아이디,
//       "c_hash":"",
//       "nonce_supported":true,
//       "email_verified":"true",
//       "auth_time":1699270438,
//       "iss":"https:\/\/appleid.apple.com",
//       "is_private_email":"true",
//       "exp":1699356838,
//       "iat":1699270438,
//       "email":이메일
//       }

    public void reportWithdrawalToApple(String appleAccessToken) {

        String clientSecret = createClientSecret();

        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("token", appleAccessToken)
                .add("client_id", APPLE_BUNDLE_ID)
                .add("client_secret", clientSecret)
                .build();

        Request request = new Request.Builder()
                .url(APPLE_REVOKE_URL)
                .post(formBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        try{
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                System.out.println(response.body().string());
                System.out.println("애플 회원탈퇴 성공");
            } else {
                System.out.println("애플 회원탈퇴 실패: " + response.code());
                throw new UnauthorizedException(ErrorStatus.FAIL_TO_WITHDRAW_APPLE_SOCIAL_EXCEPTION,
                        ErrorStatus.FAIL_TO_WITHDRAW_APPLE_SOCIAL_EXCEPTION.getMessage());
            }
        }catch (Exception e){
            throw new UnauthorizedException(ErrorStatus.FAIL_TO_WITHDRAW_APPLE_SOCIAL_EXCEPTION,
                    ErrorStatus.FAIL_TO_WITHDRAW_APPLE_SOCIAL_EXCEPTION.getMessage());
        }

    }

    private String createClientSecret() {

        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "ES256");
        headers.put("kid", APPLE_KEY_ID);

        Map<String, Object> claims = new HashMap<>();
        claims.put("iss", APPLE_TEAM_ID);
        claims.put("iat", Instant.now().getEpochSecond());
        claims.put("exp", Instant.now().plusSeconds(120).getEpochSecond());
        claims.put("aud", APPLE_ISSUE_URL);
        claims.put("sub", APPLE_BUNDLE_ID);

        return Jwts.builder()
                .setHeader(headers)
                .setClaims(claims)
                .signWith(PRIVATE_KEY, SignatureAlgorithm.ES256)
                .compact();

    }


}
