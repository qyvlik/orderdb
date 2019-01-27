package io.github.qyvlik.orderdb.modules.queueup;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import io.github.qyvlik.jsonrpclite.core.handle.WebSocketSessionContainer;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.request.RequestObject;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.response.ResponseError;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.response.ResponseObject;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.method.RpcMethod;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.method.RpcParamCheckError;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.method.RpcParams;
import io.github.qyvlik.orderdb.entity.QueueUpRecord;
import io.github.qyvlik.orderdb.entity.async.AppendNotify;
import io.github.qyvlik.orderdb.modules.executor.WritableExecutor;
import io.github.qyvlik.orderdb.entity.param.JSONObjectParam;
import io.github.qyvlik.orderdb.entity.param.StringParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.concurrent.Executor;

@Service
public class AppendMethod extends RpcMethod {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    @Qualifier("recordPushExecutor")
    private Executor recordPushExecutor;
    @Autowired
    @Qualifier("webSocketSessionContainer")
    private WebSocketSessionContainer webSocketSessionContainer;
    @Autowired
    @Qualifier("writableExecutor")
    private WritableExecutor writableExecutor;

    @Autowired
    private QueueUpService queueUpService;

    public AppendMethod() {
        super("orderdb", "append", new RpcParams(
                Lists.newArrayList(
                        new StringParam("scope"),
                        new StringParam("key"),
                        new JSONObjectParam("data")
                )));
    }

    public ResponseObject<Long> orderDBAppend(String scope, String key, Object data) {
        ResponseObject<Long> responseObject = new ResponseObject<>();

        try {
            QueueUpRecord record = queueUpService.append(scope, key, data);

            if (record != null) {
                responseObject.setResult(record.getIndex());
                recordPushExecutor.execute(new AppendNotify(webSocketSessionContainer, record));
            } else {
                responseObject.setError(new ResponseError(500, "sequence failure"));
            }
        } catch (Exception e) {
            logger.error("method:{}, scope:{}, key:{}, object:{}, error:{}",
                    getMethod(), scope, key, data, e.getMessage());
            responseObject.setError(new ResponseError(500, e.getMessage()));
        }

        return responseObject;
    }

    @Override
    public Executor getExecutorByRequest(RequestObject requestObject) {
        RpcParamCheckError checkParamResult = checkParams(requestObject);

        if (checkParamResult != null) {
            return null;
        }

        return writableExecutor.getByScope(requestObject.getParams().get(0).toString());
    }

    @Override
    protected ResponseObject callInternal(WebSocketSession session, RequestObject requestObject) {

        List params = requestObject.getParams();

        String scope = params.get(0).toString();
        String key = params.get(1).toString();
        JSONObject data = (JSONObject) params.get(2);

        return orderDBAppend(scope, key, data);
    }
}
