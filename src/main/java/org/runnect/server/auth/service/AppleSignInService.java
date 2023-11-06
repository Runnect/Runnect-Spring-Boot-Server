package org.runnect.server.auth.service;


import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import org.runnect.server.auth.dto.response.SocialInfoResponseDto;
import org.runnect.server.common.constant.ErrorStatus;
import org.runnect.server.common.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.nimbusds.jwt.SignedJWT;

import java.text.ParseException;


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

    public SocialInfoResponseDto getSocialInfo(String idToken) {


        // 클라에서 준 인증토큰이 정말 애플에서 발급받은게 맞는지 확인

        try{
            //1. idToken을 parse
            SignedJWT jwt = SignedJWT.parse(idToken);
            JWTClaimsSet claimsSet = jwt.getJWTClaimsSet();

            // 발급처, aud, 시간제한, 이메일 검증

            //만료된경우
            if (System.currentTimeMillis() < claimsSet.getExpirationTime().getTime()) {
                throw new UnauthorizedException(ErrorStatus.APPLE_ID_TOKEN_TIME_EXPIRED_EXCEPTION,
                        ErrorStatus.APPLE_ID_TOKEN_TIME_EXPIRED_EXCEPTION.getMessage());
            }

            // 발급처, aud,이메일 검증 실패시
            if (!claimsSet.getIssuer().equals(APPLE_ISSUE_URL) ||
                    !claimsSet.getAudience().get(0).equals(APPLE_BUNDLE_ID) ||
                    !Boolean.parseBoolean(claimsSet.getStringClaim("email_verified"))) {
                throw new UnauthorizedException(ErrorStatus.INVALID_APPLE_ID_TOKEN_EXCEPTION,
                        ErrorStatus.INVALID_APPLE_ID_TOKEN_EXCEPTION.getMessage());

            }

            return SocialInfoResponseDto.of(claimsSet.getStringClaim("email"), claimsSet.getSubject());

        }catch (ParseException e){
            throw new UnauthorizedException(ErrorStatus.INVALID_APPLE_ID_TOKEN_EXCEPTION,
                    ErrorStatus.INVALID_APPLE_ID_TOKEN_EXCEPTION.getMessage());
        }

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


}
