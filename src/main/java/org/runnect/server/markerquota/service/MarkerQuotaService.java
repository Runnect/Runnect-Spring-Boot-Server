package org.runnect.server.markerquota.service;

import lombok.RequiredArgsConstructor;
import org.runnect.server.common.constant.ErrorStatus;
import org.runnect.server.common.exception.ConflictException;
import org.runnect.server.common.exception.NotFoundException;
import org.runnect.server.markerquota.dto.response.GrantMarkerRewardResponseDto;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MarkerQuotaService {

    private final UserRepository userRepository;
    private final MarkerRewardGrantExecutor markerRewardGrantExecutor;

    @Transactional(readOnly = true)
    public int getBalance(Long userId) {
        return findUser(userId).getMarkerQuotaBalance();
    }

    /**
     * amount만큼 마커 재화를 소비한다(코스 저장 시점에 호출). 잔액 확인과 차감이 UserRepository의
     * 조건부 UPDATE 한 문장으로 원자적으로 처리되므로, 같은 유저의 요청이 겹쳐도 잔액이 실제보다
     * 잘못 계산될 여지가 없다. 갱신된 행이 없으면(잔액 부족) 409로 알린다.
     */
    @Transactional
    public int consume(Long userId, int amount) {
        int updatedRows = userRepository.consumeMarkerQuota(userId, amount);
        if (updatedRows == 0) {
            throw new ConflictException(
                ErrorStatus.INSUFFICIENT_MARKER_QUOTA_EXCEPTION,
                ErrorStatus.INSUFFICIENT_MARKER_QUOTA_EXCEPTION.getMessage());
        }
        return findUser(userId).getMarkerQuotaBalance();
    }

    /**
     * 광고 시청 보상을 지급한다. 같은 rewardTransactionId로 중복 요청이 들어오면
     * MarkerRewardGrantExecutor의 유니크 제약 위반으로 두 번째부터는 조용히 무시(granted=false)된다.
     *
     * 이 메서드는 의도적으로 @Transactional을 붙이지 않는다 — 그래야 markerRewardGrantExecutor.grant()
     * (REQUIRES_NEW)가 이 스레드의 유일한 트랜잭션으로 실행되고 끝나며, 그 뒤에 잔액 조회가 별도의
     * 새 트랜잭션으로 열린다. 이 메서드에 @Transactional을 붙이면 그 트랜잭션의 커넥션을 쥔 채로
     * REQUIRES_NEW 커넥션을 하나 더 요구하게 돼, 요청이 몰릴 때 커넥션 풀이 두 배 빠르게 고갈된다.
     */
    public GrantMarkerRewardResponseDto grantFromAd(Long userId, int amount, String rewardTransactionId) {
        boolean granted = true;
        try {
            markerRewardGrantExecutor.grant(userId, rewardTransactionId, amount);
        } catch (DataIntegrityViolationException e) {
            granted = false;
        }
        return GrantMarkerRewardResponseDto.of(granted, getBalance(userId));
    }

    private RunnectUser findUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(
                ErrorStatus.NOT_FOUND_USER_EXCEPTION, ErrorStatus.NOT_FOUND_USER_EXCEPTION.getMessage()));
    }
}
