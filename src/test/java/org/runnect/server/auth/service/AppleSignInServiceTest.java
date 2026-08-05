package org.runnect.server.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.runnect.server.common.exception.UnauthorizedException;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * AppleSignInService는 OkHttpClient/JWKS 서명 검증기를 메서드 내부에서 직접 생성해서
 * (의존성 주입이 안 되어 있어서) 실제 애플 서버 통신 없이는 getSocialInfo()의
 * 정상 경로(서명 검증 성공)를 순수 단위 테스트로 재현할 수 없다.
 * 네트워크 없이 검증 가능한 두 가지 — P8 키 파싱, 잘못된 idToken 형식 — 만 다룬다.
 */
class AppleSignInServiceTest {

    private String base64EncodedEcPrivateKey() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec("secp256r1"));
        KeyPair keyPair = generator.generateKeyPair();
        return Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
    }

    @Test
    void 유효한_EC_비밀키면_정상적으로_파싱된다() throws Exception {
        AppleSignInService service = new AppleSignInService();

        ReflectionTestUtils.invokeMethod(service, "getPrivateKey", base64EncodedEcPrivateKey());

        assertThat(ReflectionTestUtils.getField(service, "PRIVATE_KEY")).isNotNull();
    }

    @Test
    void 잘못된_형식의_비밀키면_UnauthorizedException() {
        AppleSignInService service = new AppleSignInService();

        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(service, "getPrivateKey", "not-a-valid-key"))
            .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void idToken이_JWT_형식이_아니면_UnauthorizedException() {
        AppleSignInService service = new AppleSignInService();

        assertThatThrownBy(() -> service.getSocialInfo("this-is-not-a-jwt"))
            .isInstanceOf(UnauthorizedException.class);
    }
}
