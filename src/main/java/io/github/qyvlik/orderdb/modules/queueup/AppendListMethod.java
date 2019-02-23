package io.github.qyvlik.orderdb.modules.queueup;

import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Lists;
import io.github.qyvlik.jsonrpclite.core.handle.WebSocketSessionContainer;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.annotation.RpcMethod;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.annotation.RpcService;
import io.github.qyvlik.orderdb.entity.QueueUpRecord;
import io.github.qyvlik.orderdb.entity.async.AppendNotify;
import io.github.qyvlik.orderdb.entity.request.AppendListRequest;
import io.github.qyvlik.orderdb.entity.request.AppendRequest;
import io.github.qyvlik.orderdb.entity.response.SimpleAppendResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Executor;

@RpcService
@Service
public class AppendListMethod {

    @Autowired
    @Qualifier("recordPushExecutor")
    private Executor recordPushExecutor;
    @Autowired
    @Qualifier("webSocketSessionContainer")
    private WebSocketSessionContainer webSocketSessionContainer;

    @Autowired
    private QueueUpService queueUpService;

    @RpcMethod(group = "orderdb", value = "append.list")
    public List<SimpleAppendResult> orderDBAppendList(String scope, Boolean ignoreExist, JSONArray list) {
        List<AppendRequest> requestList = list.toJavaList(AppendRequest.class);

        List<QueueUpRecord> recordList = queueUpService.appendList(new AppendListRequest(scope, ignoreExist, requestList));
        List<SimpleAppendResult> resultList = Lists.newLinkedList();
        for (QueueUpRecord record : recordList) {
            recordPushExecutor.execute(new AppendNotify(webSocketSessionContainer, record));
            resultList.add(new SimpleAppendResult(record.getScope(), record.getKey(), record.getIndex()));
        }

        return resultList;
    }
}
