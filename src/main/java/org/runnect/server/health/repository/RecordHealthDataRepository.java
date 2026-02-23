package org.runnect.server.health.repository;

import org.runnect.server.health.entity.RecordHealthData;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RecordHealthDataRepository extends Repository<RecordHealthData, Long> {

    void save(RecordHealthData recordHealthData);

    Optional<RecordHealthData> findByRecordId(Long recordId);

    boolean existsByRecordId(Long recordId);

    void deleteByRecordId(Long recordId);

    @Query("SELECT h FROM RecordHealthData h LEFT JOIN FETCH h.heartRateSamples WHERE h.record.id = :recordId")
    Optional<RecordHealthData> findByRecordIdWithSamples(@Param("recordId") Long recordId);

    @Query("SELECT COUNT(r.id), COUNT(h.id), AVG(h.avgHeartRate), AVG(h.calories), SUM(h.calories), " +
            "COALESCE(SUM(h.zone1Seconds), 0), COALESCE(SUM(h.zone2Seconds), 0), " +
            "COALESCE(SUM(h.zone3Seconds), 0), COALESCE(SUM(h.zone4Seconds), 0), " +
            "COALESCE(SUM(h.zone5Seconds), 0) " +
            "FROM Record r LEFT JOIN RecordHealthData h ON r.id = h.record.id " +
            "WHERE r.runnectUser.id = :userId AND r.createdAt >= :startDate AND r.createdAt < :endDate")
    Object[] getHealthSummary(@Param("userId") Long userId,
                              @Param("startDate") LocalDateTime startDate,
                              @Param("endDate") LocalDateTime endDate);
}
