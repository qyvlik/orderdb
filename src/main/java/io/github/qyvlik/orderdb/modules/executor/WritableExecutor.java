package io.github.qyvlik.orderdb.modules.executor;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WritableExecutor {

    private Map<String, Executor> executorMap = Maps.newConcurrentMap();

    public Executor getByGroup(String group) {
        return executorMap.computeIfAbsent(group, k -> new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>()));
    }
}
