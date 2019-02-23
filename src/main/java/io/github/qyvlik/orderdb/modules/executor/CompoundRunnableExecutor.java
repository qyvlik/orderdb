package io.github.qyvlik.orderdb.modules.executor;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CompoundRunnableExecutor {
    private final Lock lock;
    private Queue<CompoundRunnable> runnableQueue;
    private Executor executor;
    private RunnableCombiner runnableCombiner;
    private int maxCombineSize;

    public CompoundRunnableExecutor(Executor executor, RunnableCombiner runnableCombiner, int maxCombineSize) {
        this.lock = new ReentrantLock();
        this.runnableQueue = new LinkedBlockingQueue<CompoundRunnable>();
        this.executor = executor;
        this.runnableCombiner = runnableCombiner;
        this.maxCombineSize = maxCombineSize;
    }

    public void execute(CompoundRunnable compoundRunnable) {
        this.runnableQueue.add(compoundRunnable);
        do {
            if (!tryCombineAndExecute()) {
                break;
            }
        } while (!this.runnableQueue.isEmpty());
    }

    private boolean tryCombineAndExecute() {
        if (!lock.tryLock()) {
            return false;
        }

        try {
            List<CompoundRunnable> batchRunnableList = Lists.newLinkedList();

            while (!this.runnableQueue.isEmpty()) {
                CompoundRunnable runnable = this.runnableQueue.poll();
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

    public interface RunnableCombiner {
        CompoundRunnable combine(List<CompoundRunnable> compoundRunnableList);
    }

    public interface CompoundRunnable extends Runnable {
    }
}
