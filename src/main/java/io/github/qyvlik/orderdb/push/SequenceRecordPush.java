package io.github.qyvlik.orderdb.push;

import com.alibaba.fastjson.JSON;
import io.github.qyvlik.jsonrpclite.core.common.TaskQueue;
import io.github.qyvlik.jsonrpclite.core.handle.WebSocketSessionContainer;
import io.github.qyvlik.jsonrpclite.core.jsonsub.pub.ChannelMessage;
import io.github.qyvlik.jsonrpclite.core.jsonsub.sub.ChannelSession;
import io.github.qyvlik.orderdb.entity.SequenceRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

import java.util.List;

@Service
public class SequenceRecordPush extends TaskQueue<SequenceRecord> {

    @Autowired
    @Qualifier("webSocketSessionContainer")
    private WebSocketSessionContainer webSocketSessionContainer;

    @Override
    public String group() {
        return "orderdb";
    }

    @Override
    public boolean submit(SequenceRecord record) {
        return super.submit(record);
    }

    @Override
    public void execTask(SequenceRecord record) {
        List<ChannelSession> sessionList =
                webSocketSessionContainer.getSessionListFromChannel("sub.sequence");

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

            ChannelMessage<SequenceRecord> message = new ChannelMessage<>();
            message.setChannel("sub.sequence");
            message.setResult(record);

            webSocketSessionContainer.safeSend(
                    session.getWebSocketSession(),
                    new TextMessage(JSON.toJSONString(message)));
        }

    }
}
