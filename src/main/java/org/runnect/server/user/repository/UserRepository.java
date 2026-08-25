package org.runnect.server.user.repository;

import java.util.Optional;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.entity.SocialType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<RunnectUser, Long> {
    // CREATE

    // READ
    Optional<RunnectUser> findById(Long id);

    @Query("select u from RunnectUser u where u.id = :userId")
    Optional<RunnectUser> findUserByIdWithUserStamps(@Param("userId") Long userId);

    boolean existsByNickname(String nickname);

    boolean existsByEmailAndProvider(String email, SocialType provider);

    Optional<RunnectUser> findByEmailAndProvider(String email, SocialType provider);

    // UPDATE

    /**
     * 마커 재화 소비. "잔액 조회 -> 비교 -> 차감"을 나누지 않고 조건(잔액 >= amount)과 차감을
     * 하나의 UPDATE 문에 담아 처리한다 — 같은 유저의 요청이 짧은 시간에 겹쳐도 이 문장 자체가
     * DB에서 원자적으로 실행되어 잔액이 잘못 계산되거나 음수로 내려갈 여지가 없다. 반환값이
     * 0이면 잔액 부족으로 차감이 일어나지 않은 것이다.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE RunnectUser u SET u.markerQuotaBalance = u.markerQuotaBalance - :amount "
        + "WHERE u.id = :userId AND u.markerQuotaBalance >= :amount")
    int consumeMarkerQuota(@Param("userId") Long userId, @Param("amount") int amount);

    /**
     * 마커 재화 적립(광고 시청 보상). 같은 리워드가 중복 지급되지 않도록 하는 멱등성 보장은
     * 호출부(MarkerRewardGrantExecutor)의 MarkerRewardClaim 유니크 제약이 담당하고, 이 쿼리는
     * 실제 잔액 증가만 원자적으로 수행한다.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE RunnectUser u SET u.markerQuotaBalance = u.markerQuotaBalance + :amount "
        + "WHERE u.id = :userId")
    int grantMarkerQuota(@Param("userId") Long userId, @Param("amount") int amount);

    // DELETE

}
