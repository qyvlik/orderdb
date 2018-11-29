package io.github.qyvlik.orderdb.config;

import io.github.qyvlik.jsonrpclite.core.handle.WebSocketDispatch;
import io.github.qyvlik.jsonrpclite.core.handle.WebSocketFilter;
import io.github.qyvlik.jsonrpclite.core.handle.WebSocketSessionContainer;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.method.RpcMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class BeanConfig {

    @Autowired
    private List<RpcMethod> rpcMethodList;

    @Autowired
    private List<WebSocketFilter> filters;

    @Bean("webSocketSessionContainer")
    public WebSocketSessionContainer webSocketSessionContainer() {
        return new WebSocketSessionContainer();
    }

    @Bean("executor")
    public Executor executor() {
        return Executors.newFixedThreadPool(4);
    }

    @Bean("orderdb")
    public WebSocketDispatch orderdb(@Autowired Executor executor,
                                     @Autowired WebSocketSessionContainer webSocketSessionContainer) {
        WebSocketDispatch webSocketDispatch = new WebSocketDispatch();

        webSocketDispatch.setGroup("orderdb");
        webSocketDispatch.setExecutor(executor);
        webSocketDispatch.setWebSocketSessionContainer(webSocketSessionContainer);
        webSocketDispatch.addRpcMethodList(rpcMethodList);
        webSocketDispatch.addFilterList(filters);

        return webSocketDispatch;
    }

}
