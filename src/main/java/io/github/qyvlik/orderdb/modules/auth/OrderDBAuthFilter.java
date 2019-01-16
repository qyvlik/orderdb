package io.github.qyvlik.orderdb.modules.auth;

import io.github.qyvlik.jsonrpclite.core.handle.WebSocketFilter;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.request.RequestObject;
import io.github.qyvlik.jsonrpclite.core.jsonsub.sub.SubRequestObject;
import io.github.qyvlik.orderdb.modules.auth.AuthSessionContainer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

@Service
public class OrderDBAuthFilter extends WebSocketFilter {

    @Autowired
    private AuthSessionContainer authSessionContainer;

    @Value("${orderdb.admin.checkPassword}")
    private Boolean orderAdminCheckPassword;

    public OrderDBAuthFilter() {
        setGroup("orderdb");
    }

    @Override
    public boolean filter(WebSocketSession session, RequestObject requestObject) {
        if (orderAdminCheckPassword == null || !orderAdminCheckPassword) {
            return true;
        }
        if (StringUtils.isNotBlank(requestObject.getMethod())
                && requestObject.getMethod().equals("auth")) {
            return true;
        }
        return authSessionContainer.isAuth(session);
    }

    @Override
    public boolean filter(WebSocketSession session, SubRequestObject subRequestObject) {
        if (orderAdminCheckPassword == null || !orderAdminCheckPassword) {
            return true;
        }
        return authSessionContainer.isAuth(session);
    }
}
