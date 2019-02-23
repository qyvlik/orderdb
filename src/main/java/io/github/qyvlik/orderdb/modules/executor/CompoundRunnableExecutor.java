package io.github.qyvlik.orderdb.modules.executor;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CompoundRunnableExecutor {
    private final Lock lock;
    private final LinkedBlockingQueue<CompoundRunnable> runnableQueue;
    private Executor executor;
    private RunnableCombiner runnableCombiner;
    private int maxCombineSize;
    private long maxCombineTimeout;

    public CompoundRunnableExecutor(Executor executor, RunnableCombiner runnableCombiner, int maxCombineSize, long maxCombineTimeout) {
        this.lock = new ReentrantLock();
        this.runnableQueue = new LinkedBlockingQueue<CompoundRunnable>();
        this.executor = executor;
        this.runnableCombiner = runnableCombiner;
        this.maxCombineSize = maxCombineSize;
        this.maxCombineTimeout = maxCombineTimeout;
    }

    public int getRunnableQueueSize() {
        return runnableQueue.size();
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public RunnableCombiner getRunnableCombiner() {
        return runnableCombiner;
    }

    public void setRunnableCombiner(RunnableCombiner runnableCombiner) {
        this.runnableCombiner = runnableCombiner;
    }

    public int getMaxCombineSize() {
        return maxCombineSize;
    }

    public void setMaxCombineSize(int maxCombineSize) {
        this.maxCombineSize = maxCombineSize;
    }

    public long getMaxCombineTimeout() {
        return maxCombineTimeout;
    }

    public void setMaxCombineTimeout(long maxCombineTimeout) {
        this.maxCombineTimeout = maxCombineTimeout;
    }

    public void execute(CompoundRunnable compoundRunnable) throws InterruptedException {
        this.runnableQueue.add(compoundRunnable);
        do {
            if (!tryCombineAndExecute()) {
                break;
            }
        } while (!this.runnableQueue.isEmpty());
    }

    private boolean tryCombineAndExecute() throws InterruptedException {
        if (!lock.tryLock()) {
            return false;
        }

        try {
            List<CompoundRunnable> batchRunnableList = Lists.newLinkedList();

            while (true) {
                CompoundRunnable runnable = this.runnableQueue.poll(this.maxCombineTimeout, TimeUnit.MILLISECONDS);
                if (runnable == null) {
                    break;
                }
                batchRunnableList.add(runnable);
                if (batchRunnableList.size() >= this.maxCombineSize) {
                    break;
                }
            }

            if (batchRunnableList.isEmpty()) {
                return true;
            }

            CompoundRunnable compoundRunnable =
                    this.runnableCombiner.combine(batchRunnableList);

            this.executor.execute(compoundRunnable);

            return true;
        } finally {
            lock.unlock();
        }
    }
}
