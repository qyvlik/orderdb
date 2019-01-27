package io.github.qyvlik.orderdb.modules.sys;

import com.google.common.collect.Lists;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.request.RequestObject;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.response.ResponseObject;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.method.RpcMethod;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.method.RpcParams;
import io.github.qyvlik.orderdb.modules.executor.WritableExecutor;
import io.github.qyvlik.orderdb.entity.param.StringParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class SysStateMethod extends RpcMethod {

    @Autowired
    @Qualifier("writableExecutor")
    private WritableExecutor writableExecutor;

    public SysStateMethod() {
        super("orderdb",
                "sys.state",
                new RpcParams(Lists.newArrayList(
                        new StringParam("scope")
                ))
        );
    }

    public ResponseObject<String> writePendingState(String scope) {
        ResponseObject<String> responseObject = new ResponseObject<>();

        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) writableExecutor.getByScope(scope);

        int pendingSize = threadPoolExecutor.getQueue().size();

        responseObject.setResult("scope: " + scope + " have pending size:" + pendingSize);

        return responseObject;
    }

    @Override
    protected ResponseObject callInternal(WebSocketSession session, RequestObject requestObject) {
        List params = requestObject.getParams();
        String scope = params.get(0).toString();
        return writePendingState(scope);
    }
}
