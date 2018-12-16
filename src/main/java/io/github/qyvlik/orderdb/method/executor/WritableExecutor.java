package io.github.qyvlik.orderdb.method.executor;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class WritableExecutor {

    private Map<String, Executor> executorMap = Maps.newConcurrentMap();

    public Executor getByGroup(String group) {
        return executorMap.computeIfAbsent(group, k -> Executors.newSingleThreadExecutor());
    }
}
