package org.runnect.server.markerquota.repository;

import org.runnect.server.markerquota.entity.MarkerRewardClaim;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarkerRewardClaimRepository extends JpaRepository<MarkerRewardClaim, Long> {
}
