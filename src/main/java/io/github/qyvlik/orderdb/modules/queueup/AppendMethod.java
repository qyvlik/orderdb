package io.github.qyvlik.orderdb.modules.queueup;

import io.github.qyvlik.jsonrpclite.core.handle.WebSocketSessionContainer;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.annotation.RpcMethod;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.annotation.RpcService;
import io.github.qyvlik.orderdb.entity.QueueUpRecord;
import io.github.qyvlik.orderdb.entity.async.AppendNotify;
import io.github.qyvlik.orderdb.modules.executor.WritableExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;

@RpcService
@Service
public class AppendMethod {

    @Autowired
    @Qualifier("recordPushExecutor")
    private Executor recordPushExecutor;
    @Autowired
    @Qualifier("webSocketSessionContainer")
    private WebSocketSessionContainer webSocketSessionContainer;
    @Autowired
    @Qualifier("writableExecutor")
    private WritableExecutor writableExecutor;

    @Autowired
    private QueueUpService queueUpService;

    @RpcMethod(group = "orderdb", value = "append")
    public Long orderDBAppend(String scope, String key, Object data) {
        Long result = null;

        QueueUpRecord record = queueUpService.append(scope, key, data);

        if (record != null) {
            result = record.getIndex();
            recordPushExecutor.execute(new AppendNotify(webSocketSessionContainer, record));
        } else {
            throw new RuntimeException("sequence failure");
        }
        return result;
    }

}
