package io.github.qyvlik.orderdb.modules.executor;

import java.util.List;

public interface RunnableCombiner {
    CompoundRunnable combine(List<CompoundRunnable> compoundRunnableList);
}
