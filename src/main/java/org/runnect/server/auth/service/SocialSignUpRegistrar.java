package org.runnect.server.auth.service;

import lombok.RequiredArgsConstructor;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.entity.SocialType;
import org.runnect.server.user.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 같은 소셜 계정으로 첫 로그인 요청이 동시에(더블탭, 타임아웃 후 재시도 등) 들어오면 AuthService.signIn()의
 * "가입 여부 확인 -> 없으면 저장" 사이에서 둘 다 "미가입"으로 보고 각자 저장을 시도할 수 있다.
 * (email, provider) 유니크 제약으로 DB가 하나는 거부하는데, 이 두 요청은 스크랩처럼 서로 다른
 * 사용자 의도가 충돌하는 게 아니라 "같은 계정으로 로그인 성공"이라는 동일한 결과를 원하므로,
 * 실패한 쪽도 조회로 넘어가 그대로 로그인에 성공시키는 게 맞다(signIn()에서 처리).
 *
 * 이 저장은 REQUIRES_NEW로 signIn()과 분리된 트랜잭션에서 실행한다. 저장이 실패하면(유니크 제약
 * 위반) 예외를 여기서 삼키지 않고 그대로 던져 이 트랜잭션이 정상적으로 롤백되게 한다 — JPA는
 * flush 실패 이후의 영속성 컨텍스트를 커밋 가능한 상태로 되돌릴 수 없어("current transaction is
 * aborted"), 캐치 후 그대로 커밋을 시도하면 UnexpectedRollbackException으로 다시 실패한다(실제
 * REQUIRES_NEW 안에서 캐치하고 커밋을 시도했다가 이 예외로 재현/확인한 뒤 지금 구조로 바꿈).
 * 대신 이 트랜잭션은 실패 시 롤백으로 깔끔히 끝내고, 예외 자체는 signIn()의 트랜잭션(이 실패와
 * 무관하게 살아있는)으로 전파시켜 거기서 잡아 무시한다.
 */
@Component
@RequiredArgsConstructor
public class SocialSignUpRegistrar {

    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void register(String nickname, String email, String socialId, SocialType provider) {
        userRepository.save(RunnectUser.builder()
            .nickname(nickname)
            .email(email)
            .socialId(socialId)
            .provider(provider)
            .build());
    }
}
