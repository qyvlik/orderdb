package io.github.qyvlik.orderdb.filter;

import io.github.qyvlik.jsonrpclite.core.handle.WebSocketFilter;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.request.RequestObject;
import io.github.qyvlik.jsonrpclite.core.jsonsub.sub.SubRequestObject;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

@Service
public class OrderDBAuthFilter extends WebSocketFilter {

    public OrderDBAuthFilter() {
        setGroup("orderdb");
    }

    @Override
    public boolean filter(WebSocketSession session, RequestObject requestObject) {
        return true;
    }

    @Override
    public boolean filter(WebSocketSession session, SubRequestObject subRequestObject) {
        return true;
    }
}
