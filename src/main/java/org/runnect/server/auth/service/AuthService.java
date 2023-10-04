package org.runnect.server.auth.service;

import lombok.RequiredArgsConstructor;
import org.runnect.server.auth.dto.request.SignInRequestDto;
import org.runnect.server.auth.dto.response.SignInResponseDto;
import org.runnect.server.common.exception.ErrorStatus;
import org.runnect.server.common.module.convert.NicknameGenerator;
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

    @Transactional
    public SignInResponseDto signIn(SignInRequestDto signInRequestDto) {
        SocialType socialType = SocialType.valueOf(signInRequestDto.getProvider());

        String email = login(socialType, signInRequestDto.getToken());

        boolean isRegistered = userRepository.existsByEmailAndProvider(email, socialType);

        if (!isRegistered) {
            RunnectUser newUser = RunnectUser.builder()
                .nickname(generateTemporaryNickname())
                .email(email)
                .provider(socialType)
                .build();

            userRepository.save(newUser);
        }

        RunnectUser user = userRepository.findByEmailAndProvider(email, socialType)
            .orElseThrow(() -> new NotFoundUserException(ErrorStatus.NOT_FOUND_USER_EXCEPTION, ErrorStatus.NOT_FOUND_USER_EXCEPTION.getMessage()));





    }



    private String login(SocialType socialType, String socialAccessToken) {
        String email = null;
        switch (socialType) {
            case GOOGLE:
                email = googleSignInService.getSocialInfo(socialAccessToken);
        }
        return email;
    }

    private String generateTemporaryNickname() {
        String nickname = NicknameGenerator.randomInitialNickname();

        while (userRepository.existsByNickname(nickname)) {
            nickname = NicknameGenerator.randomInitialNickname();
        }
        return nickname;
    }
}
