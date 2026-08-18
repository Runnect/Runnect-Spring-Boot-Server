package org.runnect.server.common.module.concurrency;

import java.util.concurrent.ThreadLocalRandom;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

/**
 * 낙관적 락(@Version) 충돌로 실패한 작업을 정해진 횟수만큼 재시도한다.
 * action은 반드시 REQUIRES_NEW 등으로 독립된 트랜잭션 단위여야 한다 — 그래야 각 시도가
 * 완전히 새로운 트랜잭션에서 최신 상태를 다시 읽고 재시도할 수 있다.
 *
 * 재시도 사이에 짧은 지터를 둔다 — 충돌한 모든 스레드가 즉시 동시에 재시도하면 서로
 * 다시 충돌할 확률이 높아지는 thundering herd 현상을 완화하기 위함이다.
 */
@Component
public class OptimisticLockRetrier {

    private static final int MAX_ATTEMPTS = 8;
    private static final int BASE_BACKOFF_MILLIS = 15;

    public void runWithRetry(Runnable action) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                action.run();
                return;
            } catch (ObjectOptimisticLockingFailureException e) {
                if (attempt == MAX_ATTEMPTS) {
                    throw e;
                }
                backoff(attempt);
            }
        }
    }

    private void backoff(int attempt) {
        int maxJitterMillis = BASE_BACKOFF_MILLIS * attempt;
        long sleepMillis = ThreadLocalRandom.current().nextLong(maxJitterMillis + 1);
        try {
            Thread.sleep(sleepMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
