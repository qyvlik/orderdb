package io.github.qyvlik.orderdb.modules.rpcclient;

import io.github.qyvlik.jsonrpclite.core.jsonsub.pub.ChannelMessage;

public interface ChannelMessageHandler {
    void handle(ChannelMessage channelMessage);
}
