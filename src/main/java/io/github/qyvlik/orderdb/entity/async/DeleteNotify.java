package io.github.qyvlik.orderdb.entity.async;

import com.alibaba.fastjson.JSON;
import io.github.qyvlik.jsonrpclite.core.handle.WebSocketSessionContainer;
import io.github.qyvlik.jsonrpclite.core.jsonsub.pub.ChannelMessage;
import io.github.qyvlik.jsonrpclite.core.jsonsub.sub.ChannelSession;
import io.github.qyvlik.orderdb.entity.QueueUpRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.socket.TextMessage;

import java.util.List;

public class DeleteNotify implements Runnable {

    private QueueUpRecord record;
    private WebSocketSessionContainer webSocketSessionContainer;

    public DeleteNotify(WebSocketSessionContainer webSocketSessionContainer, QueueUpRecord record) {
        this.record = record;
        this.webSocketSessionContainer = webSocketSessionContainer;
    }

    @Override
    public void run() {
        List<ChannelSession> sessionList =
                webSocketSessionContainer.getSessionListFromChannel("sub.delete");

        if (sessionList == null || sessionList.size() == 0) {
            return;
        }

        for (ChannelSession session : sessionList) {
            if (session.getSubRequestObject() == null) {
                continue;
            }

            if (session.getSubRequestObject().getParams() == null) {
                continue;
            }

            if (session.getSubRequestObject().getParams().size() <= 0) {
                continue;
            }

            String scope = session.getSubRequestObject().getParams().get(0).toString();
            if (StringUtils.isBlank(scope)) {
                continue;
            }

            if (!scope.equals(record.getScope())) {
                continue;
            }

            ChannelMessage<QueueUpRecord> message = new ChannelMessage<>();
            message.setChannel("sub.delete");
            message.setResult(record);

            webSocketSessionContainer.safeSend(
                    session.getWebSocketSession(),
                    new TextMessage(JSON.toJSONString(message)));
        }
    }
}