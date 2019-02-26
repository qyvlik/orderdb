package io.github.qyvlik.orderdb.modules.rpcclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class BatchRunner {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private Executor executor;
    private CountDownLatch countDownLatch;
    private AtomicInteger pendingSize;

    public BatchRunner(Executor executor, int size) {
        this.executor = executor;
        this.countDownLatch = new CountDownLatch(size);
        this.pendingSize = new AtomicInteger(size);
    }

    public int getPendingSize() {
        return this.pendingSize.get();
    }

    public void submit(Runnable runnable) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {
                    pendingSize.decrementAndGet();
                    countDownLatch.countDown();
                }
            }
        });
    }

    public void waitDone() {
        try {
            if (countDownLatch.getCount() > 0) {
                countDownLatch.await();
            }
        } catch (Exception e) {
            logger.error("waitDone await failure:{}", e.getMessage());
        }
    }

    public void waitDone(long timeoutMillis) {
        try {
            if (countDownLatch.getCount() > 0) {
                countDownLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            logger.error("waitDone await failure:timeoutMillis:{}ms, error:{}", timeoutMillis, e.getMessage());
        }
    }
}
