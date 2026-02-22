package org.runnect.server.health.entity;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.runnect.server.common.entity.AuditingTimeEntity;
import org.runnect.server.record.entity.Record;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecordHealthData extends AuditingTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false, unique = true)
    private Record record;

    @Column(nullable = false)
    private Double avgHeartRate;

    private Double maxHeartRate;

    private Double minHeartRate;

    @Column(nullable = false)
    private Double calories;

    @Column(nullable = false)
    private Integer zone1Seconds = 0;

    @Column(nullable = false)
    private Integer zone2Seconds = 0;

    @Column(nullable = false)
    private Integer zone3Seconds = 0;

    @Column(nullable = false)
    private Integer zone4Seconds = 0;

    @Column(nullable = false)
    private Integer zone5Seconds = 0;

    @Column(nullable = false)
    private Double maxHeartRateConfig = 190.0;

    @OneToMany(mappedBy = "recordHealthData", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HeartRateSample> heartRateSamples = new ArrayList<>();

    @Builder
    public RecordHealthData(Record record, Double avgHeartRate, Double maxHeartRate, Double minHeartRate,
                            Double calories, Integer zone1Seconds, Integer zone2Seconds, Integer zone3Seconds,
                            Integer zone4Seconds, Integer zone5Seconds, Double maxHeartRateConfig) {
        this.record = record;
        this.avgHeartRate = avgHeartRate;
        this.maxHeartRate = maxHeartRate;
        this.minHeartRate = minHeartRate;
        this.calories = calories;
        this.zone1Seconds = zone1Seconds;
        this.zone2Seconds = zone2Seconds;
        this.zone3Seconds = zone3Seconds;
        this.zone4Seconds = zone4Seconds;
        this.zone5Seconds = zone5Seconds;
        this.maxHeartRateConfig = maxHeartRateConfig;
    }

    public void addHeartRateSamples(List<HeartRateSample> samples) {
        for (HeartRateSample sample : samples) {
            sample.setRecordHealthData(this);
            this.heartRateSamples.add(sample);
        }
    }

    @Override
    public void updateDeletedAt() {
        throw new RuntimeException("Course를 제외한 테이블은 정상적으로 삭제됩니다.");
    }
}
