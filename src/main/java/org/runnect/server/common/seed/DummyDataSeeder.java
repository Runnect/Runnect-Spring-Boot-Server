package org.runnect.server.common.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// 성능 테스트용 더미 데이터 시더. runnect.seed-dummy-data=true 일 때만 동작하며,
// 이미 시딩됐으면 아무 것도 하지 않는다. dev 전용, prod에는 이 프로퍼티를 절대 넣지 않는다.
// ApplicationReadyEvent 이후 지연 실행: 부팅 직후(가장 메모리가 예민한 구간)에 시딩 부하가
// 겹쳐서 배포가 반복적으로 죽는 문제가 있었음 — 앱이 완전히 안정된 뒤에 실행되도록 늦춤.
// 실제 시딩은 DummyDataSeedService.seedIfNeeded()에서 트랜잭션으로 묶어서 처리한다
// (중간에 실패해도 부분 저장이 남지 않도록 — 예전엔 유저만 저장된 채로 죽어서
// 재시작마다 유니크 제약 위반으로 무한 크래시 루프에 빠졌었음).
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "runnect.seed-dummy-data", havingValue = "true")
public class DummyDataSeeder {

    private final DummyDataSeedService dummyDataSeedService;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        Thread seedThread = new Thread(this::seed, "dummy-data-seeder");
        seedThread.setDaemon(true);
        seedThread.start();
    }

    private void seed() {
        try {
            Thread.sleep(30_000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        try {
            dummyDataSeedService.seedIfNeeded();
        } catch (Exception e) {
            log.error("[DummyDataSeeder] 더미 데이터 생성 실패, 앱 기동에는 영향 없음", e);
        }
    }
}
