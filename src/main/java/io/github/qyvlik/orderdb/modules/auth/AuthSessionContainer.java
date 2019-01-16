package io.github.qyvlik.orderdb.modules.auth;

import com.google.common.collect.Maps;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

@Service
public class AuthSessionContainer {
    private Map<String, WebSocketSession> sessionMap = Maps.newConcurrentMap();

    public boolean isAuth(WebSocketSession webSocketSession) {
        if (webSocketSession == null) {
            return false;
        }
        WebSocketSession sessionInMap = sessionMap.get(webSocketSession.getId());
        if (sessionInMap == null) {
            return false;
        }
        if (!sessionInMap.isOpen()) {
            sessionMap.remove(sessionInMap.getId());
            return false;
        }

        // compare object ref
        return sessionInMap == webSocketSession;
    }

    public void setAuth(WebSocketSession webSocketSession) {
        sessionMap.computeIfAbsent(webSocketSession.getId(), k -> webSocketSession);
    }
}
