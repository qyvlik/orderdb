package io.github.qyvlik.orderdb.modules.auth;

import io.github.qyvlik.jsonrpclite.core.jsonrpc.annotation.RpcMethod;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.annotation.RpcService;
import io.github.qyvlik.orderdb.modules.durable.OrderDBFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

@RpcService
@Service
public class AuthMethod {

    @Autowired
    @Qualifier("orderDBFactory")
    private OrderDBFactory orderDBFactory;

    @Autowired
    private AuthSessionContainer authSessionContainer;

    @RpcMethod(group = "orderdb", value = "auth")
    public String auth(String username, String password, WebSocketSession session) {
        boolean result = orderDBFactory.checkPassword(username, password);
        String resultStr = "";
        if (result) {
            authSessionContainer.setAuth(session);
            resultStr = "success";
        } else {
            resultStr = "failure";
        }
        return resultStr;
    }
}
