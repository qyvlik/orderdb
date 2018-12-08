package io.github.qyvlik.orderdb.config;

import io.github.qyvlik.jsonrpclite.core.common.ITaskQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.Executor;

@Service
public class TaskStartupConfig {

    @Autowired
    private List<ITaskQueue> taskQueueList;

    @Autowired
    @Qualifier("webSocketExecutor")
    private Executor webSocketExecutor;

    @PostConstruct
    public void initTaskQueueList() {
        for (ITaskQueue taskQueue : taskQueueList) {
            webSocketExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    taskQueue.exec();
                }
            });
        }
    }
}
