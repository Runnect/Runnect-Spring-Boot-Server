package org.runnect.server.common.module.concurrency;

import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 낙관적 락(@Version) 충돌로 실패한 작업을 정해진 횟수만큼 재시도한다.
 * action은 반드시 REQUIRES_NEW 등으로 독립된 트랜잭션 단위여야 한다 — 그래야 각 시도가
 * 완전히 새로운 트랜잭션에서 최신 상태를 다시 읽고 재시도할 수 있다.
 *
 * 재시도 사이에 짧은 지터를 둔다 — 충돌한 모든 스레드가 즉시 동시에 재시도하면 서로
 * 다시 충돌할 확률이 높아지는 thundering herd 현상을 완화하기 위함이다.
 */
@Slf4j
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

    /**
     * action을 지금 실행하지 않고, 호출 시점에 진행 중인 트랜잭션이 실제로 커밋된 뒤에만
     * 실행되도록 예약한다. action 자체는 REQUIRES_NEW라 호출 시점(=원래의 runWithRetry)에
     * 즉시 별도 트랜잭션으로 독립 커밋되는데, 그 시점이 메인 트랜잭션의 저장보다 앞서면
     * 메인 저장이 나중에 실패해 롤백돼도 이 action은 이미 커밋돼 있어 되돌릴 수 없다.
     * afterCommit으로 미루면 메인 트랜잭션이 실제로 성공했을 때만 실행된다. 진행 중인
     * 트랜잭션이 없는 채로 호출되면(동기화 비활성) 예약할 커밋 시점 자체가 없으므로
     * 그 자리에서 바로 실행한다.
     *
     * 재시도가 모두 소진되는 경우(극단적인 지속 경합)는 메인 작업 자체는 이미 성공한
     * 뒤이므로, 그 실패를 클라이언트 응답의 실패로 만들지 않고 로그로만 남긴다.
     */
    public void runAfterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            runWithRetry(action);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    runWithRetry(action);
                } catch (ObjectOptimisticLockingFailureException e) {
                    log.error("커밋 후 재시도가 모두 소진되어 활동 카운터/스탬프 반영에 실패함", e);
                }
            }
        });
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
