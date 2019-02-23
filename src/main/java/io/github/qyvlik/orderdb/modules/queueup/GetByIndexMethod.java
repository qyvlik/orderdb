package io.github.qyvlik.orderdb.modules.queueup;

import io.github.qyvlik.jsonrpclite.core.jsonrpc.annotation.RpcMethod;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.annotation.RpcService;
import io.github.qyvlik.orderdb.entity.QueueUpRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@RpcService
@Service
public class GetByIndexMethod {

    @Autowired
    private QueueUpService queueUpService;

    @RpcMethod(group = "orderdb", value = "get.by.index")
    public QueueUpRecord getValueByIndex(String scope, Long sequence) {
        return queueUpService.getByScopeAndIndex(scope, sequence);
    }
}
