package org.runnect.server.health.repository;

import org.runnect.server.health.entity.HeartRateSample;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface HeartRateSampleRepository extends Repository<HeartRateSample, Long> {

    void saveAll(Iterable<HeartRateSample> samples);

    List<HeartRateSample> findByRecordHealthDataIdOrderByElapsedSecondsAsc(Long recordHealthDataId);
}
