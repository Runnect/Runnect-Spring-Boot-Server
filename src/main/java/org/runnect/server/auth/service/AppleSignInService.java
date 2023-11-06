package org.runnect.server.auth.service;


import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import org.runnect.server.auth.dto.response.SocialInfoResponseDto;
import org.runnect.server.common.constant.ErrorStatus;
import org.runnect.server.common.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import java.net.URL;
import com.nimbusds.jose.jwk.RSAKey;
import java.security.interfaces.RSAPublicKey;




@Service
@RequiredArgsConstructor
public class AppleSignInService {
    @Value("${apple.key_id}")
    private String APPLE_KEY_ID;

    @Value("${apple.team_id}")
    private String APPLE_TEAM_ID;

    @Value("${apple.bundle_id}")
    private String APPLE_BUNDLE_ID;

    @Value("${apple.auth_url}")
    private String APPLE_AUTH_URL;



    public SocialInfoResponseDto getSocialInfo(String idToken) {
        try{
            // 클라에서 준 인증토큰이 정말 애플에서 발급받은게 맞는지 확인

            //1. idToken을 parse
            SignedJWT jwt = SignedJWT.parse(idToken);
            JWSHeader header = jwt.getHeader();
            String kid = header.getKeyID();


            // apple api를 통해 공개키 3개를 받아온다.
            URL jwkSetURL = new URL(APPLE_AUTH_URL);
            JWKSet jwkSet = JWKSet.load(jwkSetURL);
            //그 중 kid와 동일한 공개키 사용
            JWK jwk = jwkSet.getKeyByKeyId(kid);


            //클라이언트로부터 받은 identity token을 decode한다.


            // Convert the JWK to an RSAKey
            RSAKey rsaKey = (RSAKey) jwk;
            RSAPublicKey publicKey = rsaKey.toRSAPublicKey();
            RSASSAVerifier verifier = new RSASSAVerifier(publicKey);

            if (!jwt.verify(verifier)) {
                new Exception();
            }


            // Verify claims (issuer, audience, expiration, etc.)
            JWTClaimsSet claimsSet = jwt.getJWTClaimsSet();
            System.out.println(claimsSet.getSubject());
            System.out.println(claimsSet.getStringClaim("email"));

            return null;
//        if (!"https://appleid.apple.com".equals(claimsSet.getIssuer()) ||
//                "your-client-id".equals(claimsSet.getAudience()) ||
//                System.currentTimeMillis() > claimsSet.getExpirationTime().getTime()) {
//            return false;
//        }
//
//        // 인증토큰 decoded
//        String subject = claimsSet.getSubject();
//        String email = claimsSet.getStringClaim("email");


        }catch (Exception e){
            new UnauthorizedException(ErrorStatus.INVALID_APPLE_ID_TOKEN_EXCEPTION,
                    ErrorStatus.INVALID_APPLE_ID_TOKEN_EXCEPTION.getMessage());


        }



        return null;

    }

    public static void main(String[] args) {
        AppleSignInService appleSignInService = new AppleSignInService();
        appleSignInService.getSocialInfo("");
    }

}
