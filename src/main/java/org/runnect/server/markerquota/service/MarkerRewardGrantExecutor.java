package org.runnect.server.markerquota.service;

import lombok.RequiredArgsConstructor;
import org.runnect.server.common.constant.ErrorStatus;
import org.runnect.server.common.exception.NotFoundException;
import org.runnect.server.markerquota.entity.MarkerRewardClaim;
import org.runnect.server.markerquota.repository.MarkerRewardClaimRepository;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 같은 광고 리워드 요청이 재시도나 콜백 중복으로 짧은 시간에 두 번 들어오면, MarkerRewardClaim의
 * (user_id, rewardTransactionId) 유니크 제약이 두 번째 저장을 거부한다. 이 저장은 REQUIRES_NEW로
 * 호출부(MarkerQuotaService)와 분리된 트랜잭션에서 실행한다 — 제약 위반 예외를 여기서 삼키지
 * 않고 그대로 던져야 이 트랜잭션이 정상적으로 롤백된다(SocialSignUpRegistrar와 동일한 이유 —
 * Postgres는 실패한 문장 이후의 트랜잭션을 "aborted" 상태로 만들어, 캐치하고 계속 진행하면
 * 커밋 시점에 UnexpectedRollbackException으로 다시 실패한다). 예외는 그대로 전파시켜
 * 호출부(MarkerQuotaService.grantFromAd)에서 잡아 "이미 지급됨"으로 처리한다.
 *
 * 호출부는 이 메서드를 감싸는 별도의 @Transactional을 두지 않는다. 만약 호출부가 트랜잭션을 이미
 * 열어둔 채로 이 REQUIRES_NEW를 호출하면, 그 스레드는 호출부의 커넥션과 이 메서드의 커넥션을
 * 동시에 점유하게 된다 — 동시 요청이 많아지면 스레드마다 커넥션을 2개씩 필요로 하게 돼 커넥션
 * 풀이 예상보다 훨씬 빨리 고갈될 수 있다(실제로 동시성 테스트에서 스레드 10개로 이 상태를
 * 재현해 확인함). 그래서 userId만 받아 이 메서드 안에서 직접 조회하고, 호출부는 이 호출이 끝난
 * 뒤에야 결과 조회용 트랜잭션을 새로 연다.
 */
@Component
@RequiredArgsConstructor
public class MarkerRewardGrantExecutor {

    private final UserRepository userRepository;
    private final MarkerRewardClaimRepository markerRewardClaimRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void grant(Long userId, String rewardTransactionId, int amount) {
        RunnectUser runnectUser = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(
                ErrorStatus.NOT_FOUND_USER_EXCEPTION, ErrorStatus.NOT_FOUND_USER_EXCEPTION.getMessage()));

        markerRewardClaimRepository.saveAndFlush(
            MarkerRewardClaim.builder()
                .runnectUser(runnectUser)
                .rewardTransactionId(rewardTransactionId)
                .grantedAmount(amount)
                .build()
        );
        userRepository.grantMarkerQuota(userId, amount);
    }
}
