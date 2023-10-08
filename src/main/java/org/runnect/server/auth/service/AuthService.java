package org.runnect.server.auth.service;

import lombok.RequiredArgsConstructor;
import org.runnect.server.auth.dto.request.SignInRequestDto;
import org.runnect.server.auth.dto.response.SignInResponseDto;
import org.runnect.server.auth.dto.response.SocialInfoResponseDto;
import org.runnect.server.common.exception.ErrorStatus;
import org.runnect.server.common.module.convert.NicknameGenerator;
import org.runnect.server.config.jwt.JwtService;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.entity.SocialType;
import org.runnect.server.user.exception.userException.NotFoundUserException;
import org.runnect.server.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final GoogleSignInService googleSignInService;
    private final JwtService jwtService;

    @Transactional
    public SignInResponseDto signIn(SignInRequestDto signInRequestDto) {
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

        String type = isRegistered ? "Login" : "Signup";
        return SignInResponseDto.of(type, socialInfo.getEmail(), accessToken, refreshToken);
    }

    private SocialInfoResponseDto getSocialInfo(SocialType socialType, String socialAccessToken) {
        SocialInfoResponseDto socialInfoResponseDto = null;
        switch (socialType) {
            case GOOGLE:
                socialInfoResponseDto = googleSignInService.getSocialInfo(socialAccessToken);
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
