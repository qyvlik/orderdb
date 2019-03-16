package io.github.qyvlik.orderdb.modules.executor;

import io.github.qyvlik.jsonrpclite.core.jsonrpc.concurrent.RpcExecutor;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.request.RequestObject;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.Executor;

public class SimpleRpcExecutor implements RpcExecutor {

    private Executor defaultExecutor;
    private WritableExecutor writableExecutor;

    public SimpleRpcExecutor(Executor defaultExecutor, WritableExecutor writableExecutor) {
        this.defaultExecutor = defaultExecutor;
        this.writableExecutor = writableExecutor;
    }

    @Override
    public Executor defaultExecutor() {
        return defaultExecutor;
    }

    @Override
    public Executor getByRequest(WebSocketSession session, RequestObject requestObject) {
        String method = requestObject.getMethod();
        if (method.startsWith("get.")) {
            return defaultExecutor;
        }

        return writableExecutor.getByScope(requestObject.getParams().get(0).toString());
    }
}
