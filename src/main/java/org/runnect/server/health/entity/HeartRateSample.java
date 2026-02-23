package org.runnect.server.health.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.runnect.server.common.entity.AuditingTimeEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HeartRateSample extends AuditingTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_health_data_id", nullable = false)
    private RecordHealthData recordHealthData;

    @Column(nullable = false)
    private Double heartRate;

    @Column(nullable = false)
    private Integer elapsedSeconds;

    @Column(nullable = false)
    private Integer zone;

    @Builder
    public HeartRateSample(RecordHealthData recordHealthData, Double heartRate, Integer elapsedSeconds, Integer zone) {
        this.recordHealthData = recordHealthData;
        this.heartRate = heartRate;
        this.elapsedSeconds = elapsedSeconds;
        this.zone = zone;
    }

    public void setRecordHealthData(RecordHealthData recordHealthData) {
        this.recordHealthData = recordHealthData;
    }

    @Override
    public void updateDeletedAt() {
        throw new RuntimeException("Course를 제외한 테이블은 정상적으로 삭제됩니다.");
    }
}
