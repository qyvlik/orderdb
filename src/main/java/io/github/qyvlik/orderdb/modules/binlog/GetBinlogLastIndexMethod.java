package io.github.qyvlik.orderdb.modules.binlog;


import io.github.qyvlik.jsonrpclite.core.jsonrpc.annotation.RpcMethod;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.annotation.RpcService;
import io.github.qyvlik.orderdb.modules.queueup.QueueUpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@RpcService
@Service
public class GetBinlogLastIndexMethod {

    @Autowired
    private QueueUpService queueUpService;

    @RpcMethod(group = "orderdb", value = "get.binlog.lastIndex")
    public Long getBinlogLastIndex(String scope) {
        return queueUpService.getBinlogLastIndexByScope(scope);
    }
}
