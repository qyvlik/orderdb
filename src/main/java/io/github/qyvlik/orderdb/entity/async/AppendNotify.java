package io.github.qyvlik.orderdb.entity.async;

import com.alibaba.fastjson.JSON;
import io.github.qyvlik.jsonrpclite.core.handle.WebSocketSessionContainer;
import io.github.qyvlik.jsonrpclite.core.jsonsub.pub.ChannelMessage;
import io.github.qyvlik.jsonrpclite.core.jsonsub.sub.ChannelSession;
import io.github.qyvlik.orderdb.entity.QueueUpRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.socket.TextMessage;

import java.util.List;

public class AppendNotify implements Runnable {

    private QueueUpRecord record;
    private WebSocketSessionContainer webSocketSessionContainer;

    public AppendNotify(WebSocketSessionContainer webSocketSessionContainer, QueueUpRecord record) {
        this.record = record;
        this.webSocketSessionContainer = webSocketSessionContainer;
    }

    @Override
    public void run() {
        List<ChannelSession> sessionList =
                webSocketSessionContainer.getSessionListFromChannel("sub.append");

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

            String group = session.getSubRequestObject().getParams().get(0).toString();
            if (StringUtils.isBlank(group)) {
                continue;
            }

            if (!group.equals(record.getGroup())) {
                continue;
            }

            ChannelMessage<QueueUpRecord> message = new ChannelMessage<>();
            message.setChannel("sub.append");
            message.setResult(record);

            webSocketSessionContainer.safeSend(
                    session.getWebSocketSession(),
                    new TextMessage(JSON.toJSONString(message)));
        }
    }
}
