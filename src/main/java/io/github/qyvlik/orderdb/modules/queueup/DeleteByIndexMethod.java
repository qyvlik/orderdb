package io.github.qyvlik.orderdb.modules.queueup;

import com.google.common.collect.Lists;
import io.github.qyvlik.jsonrpclite.core.handle.WebSocketSessionContainer;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.request.RequestObject;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.response.ResponseError;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.response.ResponseObject;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.method.RpcMethod;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.method.RpcParamCheckError;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.method.RpcParams;
import io.github.qyvlik.orderdb.entity.QueueUpRecord;
import io.github.qyvlik.orderdb.entity.async.DeleteNotify;
import io.github.qyvlik.orderdb.modules.executor.WritableExecutor;
import io.github.qyvlik.orderdb.entity.param.LongParam;
import io.github.qyvlik.orderdb.entity.param.StringParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.concurrent.Executor;

@Service
public class DeleteByIndexMethod extends RpcMethod {
    @Autowired
    private QueueUpService queueUpService;

    @Autowired
    @Qualifier("recordPushExecutor")
    private Executor recordPushExecutor;

    @Autowired
    @Qualifier("webSocketSessionContainer")
    private WebSocketSessionContainer webSocketSessionContainer;

    @Autowired
    @Qualifier("writableExecutor")
    private WritableExecutor writableExecutor;

    public DeleteByIndexMethod() {
        super("orderdb", "delete.by.index", new RpcParams(Lists.newArrayList(
                new StringParam("group"),
                new LongParam("index")
        )));
    }

    @Override
    public Executor getExecutorByRequest(RequestObject requestObject) {
        RpcParamCheckError checkParamResult = checkParams(requestObject);

        if (checkParamResult != null) {
            return null;
        }

        return writableExecutor.getByGroup(requestObject.getParams().get(0).toString());
    }

    public ResponseObject<String> deleteByIndex(String group, Long index) {
        ResponseObject<String> responseObject = new ResponseObject<String>();
        try {
            QueueUpRecord record = queueUpService.delete(group, index);
            responseObject.setResult(record != null ? "success" : "failure");
            if (record != null) {
                recordPushExecutor.execute(new DeleteNotify(webSocketSessionContainer, record));
            }
        } catch (Exception e) {
            responseObject.setError(new ResponseError(500, e.getMessage()));
        }
        return responseObject;
    }

    @Override
    protected ResponseObject callInternal(WebSocketSession session, RequestObject requestObject) {
        List params = requestObject.getParams();
        String group = params.get(0).toString();
        Long seq = Long.parseLong(params.get(1).toString());
        return deleteByIndex(group, seq);
    }
}
