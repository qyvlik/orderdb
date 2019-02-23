package io.github.qyvlik.orderdb.modules.queueup;

import io.github.qyvlik.jsonrpclite.core.jsonrpc.annotation.RpcMethod;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.annotation.RpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@RpcService
@Service
public class GetLatestSequenceMethod {

    @Autowired
    private QueueUpService queueUpService;

    @RpcMethod(group = "orderdb", value = "get.latest.index")
    public Long getLatestIndex(String scope) {
        return queueUpService.getLastIndexByScope(scope);
    }
}
