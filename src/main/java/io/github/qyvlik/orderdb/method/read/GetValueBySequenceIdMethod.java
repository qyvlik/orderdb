package io.github.qyvlik.orderdb.method.read;

import com.google.common.collect.Lists;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.request.RequestObject;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.response.ResponseError;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.response.ResponseObject;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.method.RpcMethod;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.method.RpcParams;
import io.github.qyvlik.orderdb.entity.SequenceRecord;
import io.github.qyvlik.orderdb.method.param.LongParam;
import io.github.qyvlik.orderdb.method.param.StringParam;
import io.github.qyvlik.orderdb.service.SequenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

@Service
public class GetValueBySequenceIdMethod extends RpcMethod {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SequenceService sequenceService;

    public GetValueBySequenceIdMethod() {
        super("orderdb", "orderdb.get.value.by.sequenceId", new RpcParams(
                Lists.newArrayList(
                        new StringParam("group"),
                        new LongParam("sequenceId")
                )));
    }

    public ResponseObject<SequenceRecord> getValueByUniqueKey(String group, Long sequenceId) {
        ResponseObject<SequenceRecord> responseObject = new ResponseObject<>();
        try {
            SequenceRecord record = sequenceService.get(group, sequenceId);
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
        String group = params.get(0).toString();
        Long sequenceId = Long.parseLong(params.get(1).toString());
        return getValueByUniqueKey(group, sequenceId);
    }
}
