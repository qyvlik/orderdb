package io.github.qyvlik.orderdb.config;

import io.github.qyvlik.jsonrpclite.core.handle.WebSocketDispatch;
import io.github.qyvlik.jsonrpclite.core.handle.WebSocketFilter;
import io.github.qyvlik.jsonrpclite.core.handle.WebSocketSessionContainer;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.method.RpcMethod;
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

    @Autowired
    @Qualifier("writeExecutor")
    private Executor writeExecutor;

    @Bean("webSocketSessionContainer")
    public WebSocketSessionContainer webSocketSessionContainer() {
        return new WebSocketSessionContainer();
    }

    @Bean("webSocketExecutor")
    public Executor webSocketExecutor() {
        return Executors.newFixedThreadPool(4);
    }

    @Bean("orderDBDispatch")
    public WebSocketDispatch orderDBDispatch(@Autowired Executor webSocketExecutor,
                                     @Autowired WebSocketSessionContainer webSocketSessionContainer) {

        for(RpcMethod rpcMethod :rpcMethodList) {
            if (rpcMethod.getGroup().equals("orderdb") && rpcMethod.getMethod().equals("orderdb.sequence")) {
                rpcMethod.setExecutor(writeExecutor);
            }
        }

        WebSocketDispatch webSocketDispatch = new WebSocketDispatch();

        webSocketDispatch.setGroup("orderdb");
        webSocketDispatch.setExecutor(webSocketExecutor);
        webSocketDispatch.setWebSocketSessionContainer(webSocketSessionContainer);
        webSocketDispatch.addRpcMethodList(rpcMethodList);
        webSocketDispatch.addFilterList(filters);

        return webSocketDispatch;
    }

}
