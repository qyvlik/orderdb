package io.github.qyvlik.orderdb.modules.sys;

import com.google.common.collect.Lists;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.request.RequestObject;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.response.ResponseObject;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.method.RpcMethod;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.method.RpcParams;
import io.github.qyvlik.orderdb.modules.durable.OrderDBFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

@Service
public class SysScopeListMethod extends RpcMethod {

    @Autowired
    private OrderDBFactory orderDBFactory;

    public SysScopeListMethod() {
        super("orderdb",
                "sys.scope.list",
                new RpcParams(Lists.newArrayList())
        );
    }

    public ResponseObject<List<String>> sysScopeList() {
        ResponseObject<List<String>> responseObject = new ResponseObject<List<String>>();

        responseObject.setResult(Lists.newArrayList(orderDBFactory.getDbMap().keySet()));

        return responseObject;
    }

    @Override
    protected ResponseObject callInternal(WebSocketSession session, RequestObject requestObject) {
        return sysScopeList();
    }
}
