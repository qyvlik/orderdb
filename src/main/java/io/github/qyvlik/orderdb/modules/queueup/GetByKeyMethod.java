package io.github.qyvlik.orderdb.modules.queueup;

import io.github.qyvlik.jsonrpclite.core.jsonrpc.annotation.RpcMethod;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.annotation.RpcService;
import io.github.qyvlik.orderdb.entity.QueueUpRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@RpcService
@Service
public class GetByKeyMethod {

    @Autowired
    private QueueUpService queueUpService;

    @RpcMethod(group = "orderdb", value = "get.by.key")
    public QueueUpRecord getValueByKey(String scope, String key) {
        return queueUpService.getByScopeAndKey(scope, key);
    }
}
