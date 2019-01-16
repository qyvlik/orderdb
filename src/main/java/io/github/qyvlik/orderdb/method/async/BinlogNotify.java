package io.github.qyvlik.orderdb.method.async;

import com.alibaba.fastjson.JSON;
import io.github.qyvlik.jsonrpclite.core.handle.WebSocketSessionContainer;
import io.github.qyvlik.jsonrpclite.core.jsonsub.pub.ChannelMessage;
import io.github.qyvlik.jsonrpclite.core.jsonsub.sub.ChannelSession;
import io.github.qyvlik.orderdb.entity.QueueUpBinlog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.socket.TextMessage;

import java.util.List;

public class BinlogNotify implements Runnable {
    private QueueUpBinlog binlog;
    private WebSocketSessionContainer webSocketSessionContainer;

    public BinlogNotify(QueueUpBinlog binlog, WebSocketSessionContainer webSocketSessionContainer) {
        this.binlog = binlog;
        this.webSocketSessionContainer = webSocketSessionContainer;
    }

    @Override
    public void run() {
        List<ChannelSession> sessionList =
                webSocketSessionContainer.getSessionListFromChannel("sub.binlog");

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

            if (!group.equals(binlog.getGroup())) {
                continue;
            }

            ChannelMessage<QueueUpBinlog> message = new ChannelMessage<>();
            message.setChannel("sub.binlog");
            message.setResult(binlog);

            webSocketSessionContainer.safeSend(
                    session.getWebSocketSession(),
                    new TextMessage(JSON.toJSONString(message)));
        }
    }
}
