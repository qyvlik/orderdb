package io.github.qyvlik.orderdb.modules.sys;

import io.github.qyvlik.jsonrpclite.core.jsonrpc.annotation.RpcMethod;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.annotation.RpcService;
import io.github.qyvlik.orderdb.modules.executor.WritableExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadPoolExecutor;

@RpcService
@Service
public class SysStateMethod {

    @Autowired
    @Qualifier("writableExecutor")
    private WritableExecutor writableExecutor;

    @RpcMethod(group = "orderdb", value = "sys.state")
    public String writePendingState(String scope) {


        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) writableExecutor.getByScope(scope);

        int pendingSize = threadPoolExecutor.getQueue().size();


        return "scope: " + scope + " have pending size:" + pendingSize;
    }

}
