package io.github.qyvlik.orderdb.modules.queueup;

import io.github.qyvlik.jsonrpclite.core.handle.WebSocketSessionContainer;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.annotation.RpcMethod;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.annotation.RpcService;
import io.github.qyvlik.orderdb.entity.QueueUpRecord;
import io.github.qyvlik.orderdb.entity.async.DeleteNotify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;

@RpcService
@Service
public class DeleteByKeyMethod {

    @Autowired
    private QueueUpService queueUpService;

    @Autowired
    @Qualifier("recordPushExecutor")
    private Executor recordPushExecutor;

    @Autowired
    @Qualifier("webSocketSessionContainer")
    private WebSocketSessionContainer webSocketSessionContainer;

    @RpcMethod(group = "orderdb", value = "delete.by.key")
    public String deleteByKey(String scope, String key) {

        QueueUpRecord record = queueUpService.delete(scope, key);
        String result = record != null ? "success" : "failure";
        if (record != null) {
            recordPushExecutor.execute(new DeleteNotify(webSocketSessionContainer, record));
        }
        return result;
    }
}
