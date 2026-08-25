package org.runnect.server.markerquota.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Column;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.runnect.server.common.entity.AuditingTimeEntity;
import org.runnect.server.user.entity.RunnectUser;

/**
 * 영상 광고 시청 보상으로 마커 재화를 지급받은 기록. (user_id, rewardTransactionId) 유니크
 * 제약이 같은 리워드가 중복 지급되는 걸 막는 유일한 장치다 — 네트워크 재시도나 광고 SDK 콜백
 * 중복으로 같은 시청 건에 대한 지급 요청이 여러 번 들어와도, 두 번째 이후 저장은 제약 위반으로
 * 실패해 잔액이 두 번 늘어나지 않는다. 지급 성공 여부를 애플리케이션에서 먼저 조회해 판단하지
 * 않는 이유는, 조회와 저장 사이에도 같은 레이스가 생길 수 있기 때문이다 — DB 유니크 제약만이
 * 이 판단을 원자적으로 보장한다.
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
    @UniqueConstraint(
        columnNames = {"user_id", "reward_transaction_id"}
    )
})
public class MarkerRewardClaim extends AuditingTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private RunnectUser runnectUser;

    @Column(name = "reward_transaction_id", nullable = false, length = 100)
    private String rewardTransactionId;

    @Column(nullable = false)
    private int grantedAmount;

    @Builder
    public MarkerRewardClaim(RunnectUser runnectUser, String rewardTransactionId, int grantedAmount) {
        this.runnectUser = runnectUser;
        this.rewardTransactionId = rewardTransactionId;
        this.grantedAmount = grantedAmount;
    }

    @Override
    public void updateDeletedAt() {
        throw new RuntimeException("MarkerRewardClaim은 삭제되지 않는 지급 이력입니다.");
    }
}
