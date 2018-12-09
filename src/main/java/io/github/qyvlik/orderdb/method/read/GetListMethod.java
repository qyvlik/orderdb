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
public class GetListMethod extends RpcMethod {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private SequenceService sequenceService;

    public GetListMethod() {
        super("orderdb", "get.list", new RpcParams(
                Lists.newArrayList(
                        new StringParam("group"),
                        new LongParam("from"),
                        new LongParam("to")
                )));
    }

    private ResponseObject<List<SequenceRecord>> getList(String group, Long from, Long to) {
        ResponseObject<List<SequenceRecord>> responseObject = new ResponseObject<List<SequenceRecord>>();

        if (from == null || from <= 0) {
            responseObject.setError(new ResponseError(400, "from must bigger than zero"));
            return responseObject;
        }

        if (to == null || to <= 0) {
            responseObject.setError(new ResponseError(400, "to must bigger than zero"));
            return responseObject;
        }

        if (from >= to) {
            responseObject.setError(new ResponseError(400, "to must bigger than from"));
            return responseObject;
        }

        int limit = 1000;
        if (to - from > 1000) {
            responseObject.setError(new ResponseError(400, "(to - from) must less than " + limit));
            return responseObject;
        }

        long seek = from;

        List<SequenceRecord> list = Lists.newLinkedList();

        do {
            SequenceRecord sequenceRecord = sequenceService.getBySequence(group, seek);
            if (sequenceRecord != null) {
                list.add(sequenceRecord);
            }
        } while (seek++ < to);

        responseObject.setResult(list);

        return responseObject;
    }

    @Override
    protected ResponseObject callInternal(WebSocketSession session, RequestObject requestObject) {
        List params = requestObject.getParams();
        String group = params.get(0).toString();
        Long from = Long.parseLong(params.get(1).toString());
        Long to = Long.parseLong(params.get(2).toString());
        return getList(group, from, to);
    }
}