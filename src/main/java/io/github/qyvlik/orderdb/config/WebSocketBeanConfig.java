package io.github.qyvlik.orderdb.config;

import io.github.qyvlik.jsonrpclite.core.handle.WebSocketDispatch;
import io.github.qyvlik.jsonrpclite.core.handle.WebSocketFilter;
import io.github.qyvlik.jsonrpclite.core.handle.WebSocketSessionContainer;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.method.RpcMethod;
import io.github.qyvlik.orderdb.method.executor.WritableExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class WebSocketBeanConfig {

    @Autowired
    private List<RpcMethod> rpcMethodList;

    @Autowired
    private List<WebSocketFilter> filters;

    @Bean("webSocketSessionContainer")
    public WebSocketSessionContainer webSocketSessionContainer() {
        return new WebSocketSessionContainer();
    }

    @Bean("webSocketExecutor")
    public Executor webSocketExecutor() {
        return Executors.newFixedThreadPool(4);
    }

    @Bean("recordPushExecutor")
    public Executor recordPushExecutor() {
        return Executors.newSingleThreadExecutor();
    }

    @Bean("writableExecutor")
    public WritableExecutor writableExecutor() {
        return new WritableExecutor();
    }

    @Bean("orderDBDispatch")
    public WebSocketDispatch orderDBDispatch(
            @Autowired @Qualifier("webSocketExecutor") Executor webSocketExecutor,
            @Autowired @Qualifier("webSocketSessionContainer") WebSocketSessionContainer webSocketSessionContainer) {

        WebSocketDispatch webSocketDispatch = new WebSocketDispatch();

        webSocketDispatch.setGroup("orderdb");
        webSocketDispatch.setExecutor(webSocketExecutor);
        webSocketDispatch.setWebSocketSessionContainer(webSocketSessionContainer);
        webSocketDispatch.addRpcMethodList(rpcMethodList);
        webSocketDispatch.addFilterList(filters);

        return webSocketDispatch;
    }


}
