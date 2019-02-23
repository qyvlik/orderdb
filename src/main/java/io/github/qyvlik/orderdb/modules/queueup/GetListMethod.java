package io.github.qyvlik.orderdb.modules.queueup;

import com.google.common.collect.Lists;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.annotation.RpcMethod;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.annotation.RpcService;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.response.ResponseObject;
import io.github.qyvlik.orderdb.entity.QueueUpRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@RpcService
@Service
public class GetListMethod {

    @Autowired
    private QueueUpService queueUpService;

    @RpcMethod(group = "orderdb", value = "get.list")
    private List<QueueUpRecord> getList(String scope, Long from, Long to) {
        ResponseObject<List<QueueUpRecord>> responseObject = new ResponseObject<>();

        if (from == null || from < 0) {
            throw new RuntimeException("from must bigger than zero");
        }

        if (to == null || to < 0) {
            throw new RuntimeException("to must bigger than zero");
        }

        if (from >= to) {
            throw new RuntimeException("to must bigger than from");
        }

        int limit = 100;
        if (to - from > limit) {
            throw new RuntimeException("(to - from) must less than " + limit);
        }

        long seek = from;

        List<QueueUpRecord> list = Lists.newLinkedList();

        do {
            QueueUpRecord record = queueUpService.getByScopeAndIndex(scope, seek);
            if (record != null) {
                list.add(record);
            }
        } while (seek++ < to);

        return list;
    }
}
