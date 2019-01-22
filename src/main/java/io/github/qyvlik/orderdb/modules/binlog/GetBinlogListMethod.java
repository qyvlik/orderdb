package io.github.qyvlik.orderdb.modules.binlog;

import com.google.common.collect.Lists;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.request.RequestObject;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.response.ResponseError;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.response.ResponseObject;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.method.RpcMethod;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.method.RpcParams;
import io.github.qyvlik.orderdb.entity.QueueUpBinlog;
import io.github.qyvlik.orderdb.entity.param.LongParam;
import io.github.qyvlik.orderdb.entity.param.StringParam;
import io.github.qyvlik.orderdb.modules.queueup.QueueUpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

@Service
public class GetBinlogListMethod extends RpcMethod {

    @Autowired
    private QueueUpService queueUpService;

    public GetBinlogListMethod() {
        super("orderdb", "get.binlog.list", new RpcParams(
                Lists.newArrayList(
                        new StringParam("group"),
                        new LongParam("from"),
                        new LongParam("to")
                )));
    }


    public ResponseObject<List<QueueUpBinlog>> getBinlogList(String group, Long from, Long to) {
        ResponseObject<List<QueueUpBinlog>> responseObject = new ResponseObject<>();
        if (from == null || from < 0) {
            responseObject.setError(new ResponseError(400, "from must bigger than zero"));
            return responseObject;
        }

        if (to == null || to < 0) {
            responseObject.setError(new ResponseError(400, "to must bigger than zero"));
            return responseObject;
        }

        if (from >= to) {
            responseObject.setError(new ResponseError(400, "to must bigger than from"));
            return responseObject;
        }

        int limit = 100;
        if (to - from > limit) {
            responseObject.setError(new ResponseError(400, "(to - from) must less than " + limit));
            return responseObject;
        }

        long seek = from;
        List<QueueUpBinlog> list = Lists.newLinkedList();

        do {
            QueueUpBinlog record = queueUpService.getBinlogByGroupAndIndex(group, seek);
            if (record != null) {
                list.add(record);
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
        return getBinlogList(group, from, to);
    }
}