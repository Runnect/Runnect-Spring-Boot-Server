package org.runnect.server.auth.service;

import lombok.RequiredArgsConstructor;
import org.runnect.server.auth.google.GoogleSignInService;
import org.runnect.server.user.entity.SocialType;
import org.runnect.server.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final GoogleSignInService googleSignInService;





    private String login(SocialType socialType, String socialAccessToken) {
        String email = null;
        switch (socialType) {
            case GOOGLE:
                email = googleSignInService.getSocialInfo(socialAccessToken);
        }
        return email;
    }
}
