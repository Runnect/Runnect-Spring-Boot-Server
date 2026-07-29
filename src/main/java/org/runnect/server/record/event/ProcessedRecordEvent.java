package org.runnect.server.record.event;

import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

// 카프카는 at-least-once 전달만 보장해 같은 메시지가 재전달될 수 있음.
// recordId를 PK로 삼아 "이미 처리된 이벤트인지" 판별하는 멱등성 가드용 테이블.
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessedRecordEvent {

    @Id
    private Long recordId;

    public ProcessedRecordEvent(Long recordId) {
        this.recordId = recordId;
    }
}
