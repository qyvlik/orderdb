package io.github.qyvlik.orderdb.method.write;

import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Lists;
import io.github.qyvlik.jsonrpclite.core.handle.WebSocketSessionContainer;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.request.RequestObject;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.response.ResponseError;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.response.ResponseObject;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.method.RpcMethod;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.method.RpcParamCheckError;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.method.RpcParams;
import io.github.qyvlik.orderdb.entity.AppendListRequest;
import io.github.qyvlik.orderdb.entity.AppendRequest;
import io.github.qyvlik.orderdb.entity.QueueUpRecord;
import io.github.qyvlik.orderdb.entity.SimpleAppendResult;
import io.github.qyvlik.orderdb.method.async.AppendNotify;
import io.github.qyvlik.orderdb.method.executor.WritableExecutor;
import io.github.qyvlik.orderdb.method.param.ListParam;
import io.github.qyvlik.orderdb.method.param.StringParam;
import io.github.qyvlik.orderdb.service.QueueUpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.concurrent.Executor;

@Service
public class AppendListMethod extends RpcMethod {

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

    public AppendListMethod() {
        super("orderdb", "append.list", new RpcParams(
                Lists.newArrayList(
                        new StringParam("group"),
                        new StringParam("ignoreExist"),
                        new ListParam("list")
                )));
    }

    public ResponseObject<List<SimpleAppendResult>> orderDBAppendList(String group, Boolean ignoreExist, JSONArray list) {
        List<AppendRequest> requestList = list.toJavaList(AppendRequest.class);

        ResponseObject<List<SimpleAppendResult>> responseObject = new ResponseObject<>();

        try {
            List<QueueUpRecord> recordList = queueUpService.appendList(new AppendListRequest(group, ignoreExist, requestList));
            List<SimpleAppendResult> resultList = Lists.newLinkedList();
            for (QueueUpRecord record : recordList) {
                recordPushExecutor.execute(new AppendNotify(webSocketSessionContainer, record));
                resultList.add(new SimpleAppendResult(record.getGroup(), record.getKey(), record.getIndex()));
            }
            responseObject.setResult(resultList);
        } catch (Exception e) {
            logger.error("method:{}, group:{},  error:{}",
                    getMethod(), group, e.getMessage());
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

        // group
        return writableExecutor.getByGroup(requestObject.getParams().get(0).toString());
    }

    @Override
    protected ResponseObject callInternal(WebSocketSession session, RequestObject requestObject) {

        List params = requestObject.getParams();

        String group = params.get(0).toString();
        Boolean ignoreExist = Boolean.parseBoolean(params.get(1).toString());
        JSONArray list = (JSONArray) params.get(2);

        return orderDBAppendList(group, ignoreExist, list);
    }
}
