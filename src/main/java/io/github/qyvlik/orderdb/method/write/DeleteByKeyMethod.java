package io.github.qyvlik.orderdb.method.write;

import com.google.common.collect.Lists;
import io.github.qyvlik.jsonrpclite.core.handle.WebSocketSessionContainer;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.request.RequestObject;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.response.ResponseError;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.response.ResponseObject;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.method.RpcMethod;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.method.RpcParamCheckError;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.method.RpcParams;
import io.github.qyvlik.orderdb.entity.QueueUpRecord;
import io.github.qyvlik.orderdb.method.async.AppendNotify;
import io.github.qyvlik.orderdb.method.async.DeleteNotify;
import io.github.qyvlik.orderdb.method.executor.WritableExecutor;
import io.github.qyvlik.orderdb.method.param.StringParam;
import io.github.qyvlik.orderdb.service.QueueUpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.concurrent.Executor;

@Service
public class DeleteByKeyMethod extends RpcMethod {

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

    public DeleteByKeyMethod() {
        super("orderdb", "delete.by.key", new RpcParams(Lists.newArrayList(
                new StringParam("group"),
                new StringParam("key")
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

    public ResponseObject<String> deleteByKey(String group, String key) {
        ResponseObject<String> responseObject = new ResponseObject<String>();
        try {
            QueueUpRecord record = queueUpService.delete(group, key);
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
        String key = params.get(1).toString();
        return deleteByKey(group, key);
    }
}