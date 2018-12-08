package io.github.qyvlik.orderdb.method.write;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.request.RequestObject;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.response.ResponseError;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.response.ResponseObject;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.method.RpcMethod;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.method.RpcParams;
import io.github.qyvlik.orderdb.method.param.JSONObjectParam;
import io.github.qyvlik.orderdb.method.param.StringParam;
import io.github.qyvlik.orderdb.service.SequenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

@Service
public class SequenceMethod extends RpcMethod {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SequenceService sequenceService;

    public SequenceMethod() {
        super("orderdb", "sequence", new RpcParams(
                Lists.newArrayList(
                        new StringParam("group"),
                        new StringParam("key"),
                        new JSONObjectParam("data")
                )));
    }

    public ResponseObject<Long> orderDBSequence(String group, String key, Object data) {
        ResponseObject<Long> responseObject = new ResponseObject<>();

        try {
            Long sequence = sequenceService.sequence(group, key, data);
            responseObject.setResult(sequence);
        } catch (Exception e) {
            logger.error("method:{}, group:{}, key:{}, object:{}, error:{}",
                    getMethod(), group, key, data, e.getMessage());
            responseObject.setError(new ResponseError(500, e.getMessage()));
        }

        return responseObject;
    }

    @Override
    protected ResponseObject callInternal(WebSocketSession session, RequestObject requestObject) {

        List params = requestObject.getParams();

        String group = params.get(0).toString();
        String key = params.get(1).toString();
        JSONObject data = (JSONObject) params.get(2);

        return orderDBSequence(group, key, data);
    }
}
