package org.runnect.server.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.runnect.server.auth.dto.request.SignInRequestDto;
import org.runnect.server.auth.dto.response.AuthResponseDto;
import org.runnect.server.auth.dto.response.GetNewTokenResponseDto;
import org.runnect.server.auth.dto.response.SignInResponseDto;
import org.runnect.server.auth.dto.response.SignUpResponseDto;
import org.runnect.server.auth.dto.response.SocialInfoResponseDto;
import org.runnect.server.common.constant.TokenStatus;
import org.runnect.server.config.jwt.JwtService;
import org.runnect.server.config.redis.RedisService;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.entity.SocialType;
import org.runnect.server.user.exception.authException.InvalidAccessTokenException;
import org.runnect.server.user.exception.authException.InvalidRefreshTokenException;
import org.runnect.server.user.exception.authException.TimeExpiredRefreshTokenException;
import org.runnect.server.user.exception.userException.NotFoundUserException;
import org.runnect.server.user.repository.UserRepository;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private GoogleSignInService googleSignInService;
    @Mock
    private AppleSignInService appleSignInService;
    @Mock
    private KakaoSignInService kakaoSignInService;
    @Mock
    private JwtService jwtService;
    @Mock
    private RedisService redisService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, googleSignInService, appleSignInService,
            kakaoSignInService, jwtService, redisService);
    }

    private RunnectUser buildUser(Long id, String email, SocialType provider) {
        RunnectUser user = RunnectUser.builder()
            .nickname("러너" + id)
            .socialId("social-" + id)
            .email(email)
            .provider(provider)
            .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Nested
    @DisplayName("getNewToken")
    class GetNewToken {

        @Test
        @DisplayName("accessToken/refreshToken 모두 유효하고 redis에 저장된 값과 일치하면 새 accessToken을 발급한다")
        void 정상_재발급() {
            when(jwtService.verifyToken("access")).thenReturn(TokenStatus.TOKEN_VALID);
            when(jwtService.verifyToken("refresh")).thenReturn(TokenStatus.TOKEN_VALID);
            when(jwtService.getJwtContents("refresh")).thenReturn("1");
            when(redisService.getValuesByKey("1")).thenReturn("refresh");
            when(userRepository.findById(1L)).thenReturn(Optional.of(buildUser(1L, "a@runnect.io", SocialType.KAKAO)));
            when(jwtService.issuedAccessToken(1L)).thenReturn("new-access");

            GetNewTokenResponseDto response = authService.getNewToken("access", "refresh");

            assertThat(response.getAccessToken()).isEqualTo("new-access");
            assertThat(response.getRefreshToken()).isEqualTo("refresh");
        }

        @Test
        @DisplayName("accessToken이 무효하면 InvalidAccessTokenException")
        void accessToken_무효() {
            when(jwtService.verifyToken("access")).thenReturn(TokenStatus.TOKEN_INVALID);

            assertThatThrownBy(() -> authService.getNewToken("access", "refresh"))
                .isInstanceOf(InvalidAccessTokenException.class);
        }

        @Test
        @DisplayName("refreshToken이 만료됐으면 TimeExpiredRefreshTokenException")
        void refreshToken_만료() {
            when(jwtService.verifyToken("access")).thenReturn(TokenStatus.TOKEN_VALID);
            when(jwtService.verifyToken("refresh")).thenReturn(TokenStatus.TOKEN_EXPIRED);

            assertThatThrownBy(() -> authService.getNewToken("access", "refresh"))
                .isInstanceOf(TimeExpiredRefreshTokenException.class);
        }

        @Test
        @DisplayName("refreshToken이 무효하면 InvalidRefreshTokenException")
        void refreshToken_무효() {
            when(jwtService.verifyToken("access")).thenReturn(TokenStatus.TOKEN_VALID);
            when(jwtService.verifyToken("refresh")).thenReturn(TokenStatus.TOKEN_INVALID);

            assertThatThrownBy(() -> authService.getNewToken("access", "refresh"))
                .isInstanceOf(InvalidRefreshTokenException.class);
        }

        @Test
        @DisplayName("[버그 수정 검증] redis에 저장된 값이 없으면 InvalidRefreshTokenException")
        void redis에_저장된_값이_없음() {
            when(jwtService.verifyToken("access")).thenReturn(TokenStatus.TOKEN_VALID);
            when(jwtService.verifyToken("refresh")).thenReturn(TokenStatus.TOKEN_VALID);
            when(jwtService.getJwtContents("refresh")).thenReturn("1");
            when(redisService.getValuesByKey("1")).thenReturn(null);

            assertThatThrownBy(() -> authService.getNewToken("access", "refresh"))
                .isInstanceOf(InvalidRefreshTokenException.class);

            verify(jwtService, never()).issuedAccessToken(any());
        }

        @Test
        @DisplayName("[버그 수정 검증] redis에 저장된 값이 요청한 refreshToken과 다르면(재로그인 등으로 무효화됨) InvalidRefreshTokenException")
        void redis에_저장된_값과_다름() {
            when(jwtService.verifyToken("old-refresh")).thenReturn(TokenStatus.TOKEN_VALID);
            when(jwtService.verifyToken("access")).thenReturn(TokenStatus.TOKEN_VALID);
            when(jwtService.getJwtContents("old-refresh")).thenReturn("1");
            when(redisService.getValuesByKey("1")).thenReturn("new-refresh-issued-later");

            assertThatThrownBy(() -> authService.getNewToken("access", "old-refresh"))
                .isInstanceOf(InvalidRefreshTokenException.class);

            verify(jwtService, never()).issuedAccessToken(any());
        }

        @Test
        @DisplayName("refreshToken의 userId 클레임이 숫자가 아니면 NotFoundUserException")
        void 클레임이_숫자가_아님() {
            when(jwtService.verifyToken("access")).thenReturn(TokenStatus.TOKEN_VALID);
            when(jwtService.verifyToken("refresh")).thenReturn(TokenStatus.TOKEN_VALID);
            when(jwtService.getJwtContents("refresh")).thenReturn("not-a-number");

            assertThatThrownBy(() -> authService.getNewToken("access", "refresh"))
                .isInstanceOf(NotFoundUserException.class);
        }

        @Test
        @DisplayName("존재하지 않는 유저면 NotFoundUserException")
        void 존재하지_않는_유저() {
            when(jwtService.verifyToken("access")).thenReturn(TokenStatus.TOKEN_VALID);
            when(jwtService.verifyToken("refresh")).thenReturn(TokenStatus.TOKEN_VALID);
            when(jwtService.getJwtContents("refresh")).thenReturn("1");
            when(redisService.getValuesByKey("1")).thenReturn("refresh");
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.getNewToken("access", "refresh"))
                .isInstanceOf(NotFoundUserException.class);
        }
    }

    @Nested
    @DisplayName("signIn")
    class SignIn {

        @Test
        @DisplayName("신규 유저면 회원가입 처리하고 SignUpResponseDto를 반환한다")
        void 신규_회원가입() {
            when(kakaoSignInService.getSocialInfo("kakao-token")).thenReturn(
                SocialInfoResponseDto.of("new@runnect.io", "social-1"));
            when(userRepository.existsByEmailAndProvider("new@runnect.io", SocialType.KAKAO)).thenReturn(false);
            when(userRepository.existsByNickname(org.mockito.ArgumentMatchers.any())).thenReturn(false);
            RunnectUser savedUser = buildUser(1L, "new@runnect.io", SocialType.KAKAO);
            when(userRepository.findByEmailAndProvider("new@runnect.io", SocialType.KAKAO))
                .thenReturn(Optional.of(savedUser));
            when(jwtService.issuedAccessToken(1L)).thenReturn("access");
            when(jwtService.issuedRefreshToken(1L)).thenReturn("refresh");

            AuthResponseDto response = authService.signIn(new SignInRequestDto("kakao-token", "KAKAO"));

            assertThat(response).isInstanceOf(SignUpResponseDto.class);
            verify(userRepository).save(any(RunnectUser.class));
        }

        @Test
        @DisplayName("기존 유저면 로그인 처리하고 SignInResponseDto를 반환하며 새 유저를 저장하지 않는다")
        void 기존_유저_로그인() {
            when(googleSignInService.getSocialInfo("google-token")).thenReturn(
                SocialInfoResponseDto.of("existing@runnect.io", "social-2"));
            when(userRepository.existsByEmailAndProvider("existing@runnect.io", SocialType.GOOGLE)).thenReturn(true);
            RunnectUser existingUser = buildUser(2L, "existing@runnect.io", SocialType.GOOGLE);
            when(userRepository.findByEmailAndProvider("existing@runnect.io", SocialType.GOOGLE))
                .thenReturn(Optional.of(existingUser));
            when(jwtService.issuedAccessToken(2L)).thenReturn("access");
            when(jwtService.issuedRefreshToken(2L)).thenReturn("refresh");

            AuthResponseDto response = authService.signIn(new SignInRequestDto("google-token", "GOOGLE"));

            assertThat(response).isInstanceOf(SignInResponseDto.class);
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("APPLE provider면 appleSignInService로 소셜 정보를 조회한다")
        void 애플_로그인() {
            when(appleSignInService.getSocialInfo("apple-token")).thenReturn(
                SocialInfoResponseDto.of("apple@runnect.io", "social-3"));
            when(userRepository.existsByEmailAndProvider("apple@runnect.io", SocialType.APPLE)).thenReturn(true);
            RunnectUser existingUser = buildUser(3L, "apple@runnect.io", SocialType.APPLE);
            when(userRepository.findByEmailAndProvider("apple@runnect.io", SocialType.APPLE))
                .thenReturn(Optional.of(existingUser));
            when(jwtService.issuedAccessToken(3L)).thenReturn("access");
            when(jwtService.issuedRefreshToken(3L)).thenReturn("refresh");

            authService.signIn(new SignInRequestDto("apple-token", "APPLE"));

            verify(appleSignInService).getSocialInfo("apple-token");
            verify(googleSignInService, never()).getSocialInfo(any());
            verify(kakaoSignInService, never()).getSocialInfo(any());
        }

        @Test
        @DisplayName("생성된 임시 닉네임이 이미 존재하면 중복되지 않을 때까지 다시 생성한다")
        void 닉네임_중복시_재생성() {
            when(kakaoSignInService.getSocialInfo("kakao-token")).thenReturn(
                SocialInfoResponseDto.of("new@runnect.io", "social-1"));
            when(userRepository.existsByEmailAndProvider("new@runnect.io", SocialType.KAKAO)).thenReturn(false);
            when(userRepository.existsByNickname(any())).thenReturn(true, true, false);
            RunnectUser savedUser = buildUser(1L, "new@runnect.io", SocialType.KAKAO);
            when(userRepository.findByEmailAndProvider("new@runnect.io", SocialType.KAKAO))
                .thenReturn(Optional.of(savedUser));
            when(jwtService.issuedAccessToken(1L)).thenReturn("access");
            when(jwtService.issuedRefreshToken(1L)).thenReturn("refresh");

            authService.signIn(new SignInRequestDto("kakao-token", "KAKAO"));

            verify(userRepository, org.mockito.Mockito.times(3)).existsByNickname(any());
        }

        @Test
        @DisplayName("존재하지 않는 provider 값이면 IllegalArgumentException")
        void 잘못된_provider() {
            assertThatThrownBy(() -> authService.signIn(new SignInRequestDto("token", "FACEBOOK")))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
