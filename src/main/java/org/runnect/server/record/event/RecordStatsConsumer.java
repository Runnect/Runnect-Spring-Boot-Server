package org.runnect.server.record.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.runnect.server.common.constant.ErrorStatus;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.entity.StampType;
import org.runnect.server.user.exception.userException.NotFoundUserException;
import org.runnect.server.user.repository.UserRepository;
import org.runnect.server.user.service.UserStampService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecordStatsConsumer {

    private final UserRepository userRepository;
    private final UserStampService userStampService;

    // concurrency=4: 토픽 파티션 수(4개)만큼 컨슈머 스레드를 띄워, 각 스레드가
    // 파티션 하나씩 맡아 병렬로 처리한다 (파티션 수가 병렬 처리의 상한선).
    @KafkaListener(topics = "record-created", groupId = "stats-processor", concurrency = "4")
    @Transactional
    public void consume(RecordCreatedEvent event) {
        // 실제로는 가벼운 DB 갱신 작업이지만, "이 후처리가 무겁다"는 상황을
        // 실습으로 재현하기 위해 인위적으로 지연을 준다 (실제 서비스 로직 아님).
        simulateHeavyWork();

        RunnectUser user = userRepository.findById(event.getUserId())
                .orElseThrow(() -> new NotFoundUserException(
                        ErrorStatus.NOT_FOUND_USER_EXCEPTION, ErrorStatus.NOT_FOUND_USER_EXCEPTION.getMessage()));

        user.updateCreatedRecord();
        userStampService.createStampByUser(user, StampType.r);

        log.info("[Kafka] record-created 이벤트 처리 완료 (userId={}, recordId={})", event.getUserId(), event.getRecordId());
    }

    private void simulateHeavyWork() {
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
