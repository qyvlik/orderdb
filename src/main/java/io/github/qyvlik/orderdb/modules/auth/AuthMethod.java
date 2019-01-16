package io.github.qyvlik.orderdb.modules.auth;

import com.google.common.collect.Lists;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.request.RequestObject;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.response.ResponseError;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.response.ResponseObject;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.method.RpcMethod;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.method.RpcParams;
import io.github.qyvlik.orderdb.entity.param.StringParam;
import io.github.qyvlik.orderdb.modules.durable.OrderDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

@Service
public class AuthMethod extends RpcMethod {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    @Qualifier("orderDBFactory")
    private OrderDBFactory orderDBFactory;

    @Autowired
    private AuthSessionContainer authSessionContainer;

    public AuthMethod() {
        super("orderdb", "auth", new RpcParams(Lists.newArrayList(
                new StringParam("username"),
                new StringParam("password")
        )));
    }

    public ResponseObject<String> auth(String username, String password, WebSocketSession session) {
        ResponseObject<String> responseObject = new ResponseObject<String>();
        try {
            boolean result = orderDBFactory.checkPassword(username, password);
            if (result) {
                authSessionContainer.setAuth(session);
                responseObject.setResult("success");
            } else {
                responseObject.setError(new ResponseError(400, "username not exist or password invalidate"));
            }
        } catch (Exception e) {
            responseObject.setError(new ResponseError(400, "username not exist or password invalidate, " + e.getMessage()));
            logger.error("auth failure : ", e.getMessage());
        }

        return responseObject;
    }

    @Override
    protected ResponseObject callInternal(WebSocketSession session, RequestObject requestObject) {
        List params = requestObject.getParams();

        String username = params.get(0).toString();
        String password = params.get(1).toString();

        return auth(username, password, session);
    }
}
