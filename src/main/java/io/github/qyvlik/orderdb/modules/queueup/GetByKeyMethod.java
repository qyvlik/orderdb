package io.github.qyvlik.orderdb.modules.queueup;

import com.google.common.collect.Lists;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.request.RequestObject;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.response.ResponseError;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.response.ResponseObject;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.method.RpcMethod;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.method.RpcParams;
import io.github.qyvlik.orderdb.entity.QueueUpRecord;
import io.github.qyvlik.orderdb.entity.param.StringParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

@Service
public class GetByKeyMethod extends RpcMethod {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private QueueUpService queueUpService;

    public GetByKeyMethod() {
        super("orderdb", "get.by.key", new RpcParams(
                Lists.newArrayList(
                        new StringParam("scope"),
                        new StringParam("key")
                )));
    }

    public ResponseObject<QueueUpRecord> getValueByUniqueKey(String scope, String key) {
        ResponseObject<QueueUpRecord> responseObject = new ResponseObject<>();
        try {
            QueueUpRecord record = queueUpService.getByScopeAndKey(scope, key);
            responseObject.setResult(record);
        } catch (Exception e) {
            logger.error("{} failure:{}", getMethod(), e.getMessage());
            responseObject.setError(new ResponseError(500, e.getMessage()));
        }
        return responseObject;
    }

    @Override
    protected ResponseObject callInternal(WebSocketSession session, RequestObject requestObject) {
        List params = requestObject.getParams();
        String scope = params.get(0).toString();
        String key = params.get(1).toString();
        return getValueByUniqueKey(scope, key);
    }
}
