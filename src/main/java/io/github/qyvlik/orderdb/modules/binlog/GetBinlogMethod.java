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
public class GetBinlogMethod extends RpcMethod {

    @Autowired
    private QueueUpService queueUpService;

    public GetBinlogMethod() {
        super("orderdb", "get.binlog", new RpcParams(
                Lists.newArrayList(
                        new StringParam("group"),
                        new LongParam("index")
                )));
    }

    public ResponseObject<QueueUpBinlog> getBinlog(String group, Long index) {
        ResponseObject<QueueUpBinlog> responseObject = new ResponseObject<>();
        try {
            QueueUpBinlog binlog = queueUpService.getBinlogByGroupAndIndex(group, index);
            responseObject.setResult(binlog);
        } catch (Exception e) {
            responseObject.setError(new ResponseError(500, e.getMessage()));
        }
        return responseObject;
    }


    @Override
    protected ResponseObject callInternal(WebSocketSession session, RequestObject requestObject) {
        List params = requestObject.getParams();
        String group = params.get(0).toString();
        Long index = Long.parseLong(params.get(1).toString());

        return getBinlog(group, index);
    }
}
