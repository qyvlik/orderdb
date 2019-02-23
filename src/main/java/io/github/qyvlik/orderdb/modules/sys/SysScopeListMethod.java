package io.github.qyvlik.orderdb.modules.sys;

import com.google.common.collect.Lists;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.annotation.RpcMethod;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.annotation.RpcService;
import io.github.qyvlik.orderdb.modules.durable.OrderDBFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@RpcService
@Service
public class SysScopeListMethod {

    @Autowired
    private OrderDBFactory orderDBFactory;

    @RpcMethod(group = "orderdb", value = "sys.scope.list")
    public List<String> sysScopeList() {
        return Lists.newArrayList(orderDBFactory.getDbMap().keySet());
    }
}
