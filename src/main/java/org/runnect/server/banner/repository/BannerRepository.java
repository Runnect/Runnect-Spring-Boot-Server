package org.runnect.server.banner.repository;

import java.util.List;

import org.runnect.server.banner.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BannerRepository extends JpaRepository<Banner, Long> {

    List<Banner> findByIsActiveTrueOrderBySortOrderAscIdAsc();
}
