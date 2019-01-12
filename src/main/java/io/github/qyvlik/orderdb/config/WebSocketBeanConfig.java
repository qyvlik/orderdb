package io.github.qyvlik.orderdb.config;

import io.github.qyvlik.jsonrpclite.core.handle.WebSocketDispatch;
import io.github.qyvlik.jsonrpclite.core.handle.WebSocketFilter;
import io.github.qyvlik.jsonrpclite.core.handle.WebSocketSessionContainer;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.method.RpcMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class WebSocketBeanConfig {

    @Value("${orderdb.thread.read}")
    private Integer orderDbThreadRead;

    @Bean("webSocketSessionContainer")
    public WebSocketSessionContainer webSocketSessionContainer() {
        return new WebSocketSessionContainer();
    }

    @Bean("webSocketExecutor")
    public Executor webSocketExecutor() {
        if (orderDbThreadRead == null || orderDbThreadRead <= 0) {
            return Executors.newCachedThreadPool();
        }

        return Executors.newFixedThreadPool(orderDbThreadRead);
    }

    @Bean("orderDBDispatch")
    public WebSocketDispatch orderDBDispatch(
            @Autowired @Qualifier("webSocketExecutor") Executor webSocketExecutor,
            @Autowired @Qualifier("webSocketSessionContainer") WebSocketSessionContainer webSocketSessionContainer,
            @Autowired List<RpcMethod> rpcMethodList,
            @Autowired List<WebSocketFilter> filters
    ) {

        WebSocketDispatch webSocketDispatch = new WebSocketDispatch();

        webSocketDispatch.setGroup("orderdb");
        webSocketDispatch.setExecutor(webSocketExecutor);
        webSocketDispatch.setWebSocketSessionContainer(webSocketSessionContainer);
        webSocketDispatch.addRpcMethodList(rpcMethodList);
        webSocketDispatch.addFilterList(filters);

        return webSocketDispatch;
    }


}
