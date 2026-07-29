package org.runnect.server.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    // userId를 파티션 키로 써서 같은 유저의 이벤트는 항상 같은 파티션(순서 보장),
    // 다른 유저는 다른 파티션에서 병렬 처리되도록 함. 파티션 4개 = 컨슈머 최대 4개까지 병렬 확장 가능.
    @Bean
    public NewTopic recordCreatedTopic() {
        return TopicBuilder.name("record-created")
                .partitions(4)
                .replicas(1)
                .build();
    }
}
