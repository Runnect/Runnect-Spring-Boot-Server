package org.runnect.server.auth.service;

import lombok.RequiredArgsConstructor;
import org.runnect.server.auth.dto.request.SignInRequestDto;
import org.runnect.server.auth.dto.response.*;
import org.runnect.server.common.constant.ErrorStatus;
import org.runnect.server.common.constant.TokenStatus;
import org.runnect.server.common.exception.UnauthorizedException;
import org.runnect.server.common.module.convert.NicknameGenerator;
import org.runnect.server.config.jwt.JwtService;
import org.runnect.server.config.redis.RedisService;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.entity.SocialType;
import org.runnect.server.user.exception.authException.InvalidAccessTokenException;
import org.runnect.server.user.exception.authException.InvalidRefreshTokenException;
import org.runnect.server.user.exception.authException.TimeExpiredRefreshTokenException;
import org.runnect.server.user.exception.userException.NotFoundUserException;
import org.runnect.server.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final GoogleSignInService googleSignInService;
    private final AppleSignInService appleSignInService;
    private final KakaoSignInService kakaoSignInService;
    private final JwtService jwtService;
    private final RedisService redisService;

    public GetNewTokenResponseDto getNewToken(String accessToken, String refreshToken) {
        //? 토큰 에러 분기 처리(reissueToken)
        //? 1. accessToken 이상 -> 재로그인

        if (jwtService.verifyToken(accessToken) == TokenStatus.TOKEN_INVALID) {
            throw new InvalidAccessTokenException(ErrorStatus.INVALID_ACCESS_TOKEN_EXCEPTION, ErrorStatus.INVALID_ACCESS_TOKEN_EXCEPTION.getMessage());
        }

        //? 2.1 accessToken(정상또는 만료) +refreshToken (이상 또는 만료) -> 재로그인
        if (jwtService.verifyToken(refreshToken) == TokenStatus.TOKEN_EXPIRED) {
            throw new TimeExpiredRefreshTokenException(ErrorStatus.REFRESH_TOKEN_TIME_EXPIRED_EXCEPTION, ErrorStatus.REFRESH_TOKEN_TIME_EXPIRED_EXCEPTION.getMessage());
        } else if (jwtService.verifyToken(refreshToken) == TokenStatus.TOKEN_INVALID) {
            throw new InvalidRefreshTokenException(ErrorStatus.INVALID_REFRESH_TOKEN_EXCEPTION, ErrorStatus.INVALID_REFRESH_TOKEN_EXCEPTION.getMessage());
        }


        //? 2.2 accessToken(정상또는 만료) +refreshToken 정상 -> 토큰 재발급
        final String tokenContents = jwtService.getJwtContents(refreshToken);

        try {
            // refreshToken으로 유저찾기
            final long userId =  Long.parseLong(tokenContents);
            if(redisService.getValuesByKey(String.valueOf(userId)).isBlank()){
                //탈취된 refreshToken인 경우
                throw new InvalidRefreshTokenException(ErrorStatus.INVALID_REFRESH_TOKEN_EXCEPTION, ErrorStatus.INVALID_REFRESH_TOKEN_EXCEPTION.getMessage());
            }
            RunnectUser user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundUserException(ErrorStatus.NOT_FOUND_USER_EXCEPTION, ErrorStatus.NOT_FOUND_USER_EXCEPTION.getMessage()));


            // accessToken 재발급
            String newAccessToken = jwtService.issuedAccessToken(user.getId());

            return GetNewTokenResponseDto.of(newAccessToken,refreshToken);

        } catch (NumberFormatException e ) {
            throw new NotFoundUserException(ErrorStatus.NOT_FOUND_USER_EXCEPTION, ErrorStatus.NOT_FOUND_USER_EXCEPTION.getMessage());
        } catch(NullPointerException e){
            throw new InvalidRefreshTokenException(ErrorStatus.INVALID_REFRESH_TOKEN_EXCEPTION, ErrorStatus.INVALID_REFRESH_TOKEN_EXCEPTION.getMessage());
        }

    }



    @Transactional
    public AuthResponseDto signIn(SignInRequestDto signInRequestDto) {
        SocialType socialType = SocialType.valueOf(signInRequestDto.getProvider());

        SocialInfoResponseDto socialInfo = getSocialInfo(socialType, signInRequestDto.getToken());

        boolean isRegistered = userRepository.existsByEmailAndProvider(socialInfo.getEmail(), socialType);

        if (!isRegistered) {
            RunnectUser newUser = RunnectUser.builder()
                .nickname(generateTemporaryNickname())
                .email(socialInfo.getEmail())
                .socialId(socialInfo.getSocialId())
                .provider(socialType)
                .build();

            userRepository.save(newUser);
        }

        RunnectUser user = userRepository.findByEmailAndProvider(socialInfo.getEmail(), socialType)
            .orElseThrow(() -> new NotFoundUserException(ErrorStatus.NOT_FOUND_USER_EXCEPTION, ErrorStatus.NOT_FOUND_USER_EXCEPTION.getMessage()));

        String accessToken = jwtService.issuedAccessToken(user.getId());
        String refreshToken = jwtService.issuedRefreshToken(user.getId());

        if(isRegistered){
            return SignInResponseDto.of("Login", socialInfo.getEmail(), accessToken, refreshToken);
        }
        else{
            return SignUpResponseDto.of("Signup",socialInfo.getEmail(), user.getNickname(), accessToken, refreshToken);
        }

    }

    private SocialInfoResponseDto getSocialInfo(SocialType socialType, String socialAccessToken) {
        SocialInfoResponseDto socialInfoResponseDto = null;
        switch (socialType) {
            case GOOGLE:
                socialInfoResponseDto = googleSignInService.getSocialInfo(socialAccessToken);
                break;
            case APPLE:
                socialInfoResponseDto = appleSignInService.getSocialInfo(socialAccessToken);
                break;
            case KAKAO:
                socialInfoResponseDto = kakaoSignInService.getSocialInfo(socialAccessToken);
                break;
        }


        return socialInfoResponseDto;
    }

    private String generateTemporaryNickname() {
        String nickname = NicknameGenerator.randomInitialNickname();

        while (userRepository.existsByNickname(nickname)) {
            nickname = NicknameGenerator.randomInitialNickname();
        }
        return nickname;
    }
}
