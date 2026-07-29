package org.runnect.server.record.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecordEventProducer {

    private static final String TOPIC = "record-created";

    private final KafkaTemplate<String, RecordCreatedEvent> kafkaTemplate;

    // 파티션 키로 userId를 써서, 같은 유저의 기록은 항상 같은 파티션(순서 보장)으로 감
    public void publish(RecordCreatedEvent event) {
        kafkaTemplate.send(TOPIC, String.valueOf(event.getUserId()), event);
        log.info("[Kafka] record-created 이벤트 발행 (userId={}, recordId={})", event.getUserId(), event.getRecordId());
    }
}
