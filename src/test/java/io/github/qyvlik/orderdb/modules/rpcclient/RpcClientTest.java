package io.github.qyvlik.orderdb.modules.rpcclient;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.qyvlik.jsonrpclite.core.client.ChannelMessageHandler;
import io.github.qyvlik.jsonrpclite.core.client.RpcClient;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.response.ResponseObject;
import io.github.qyvlik.jsonrpclite.core.jsonsub.pub.ChannelMessage;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import java.util.concurrent.Future;

public class RpcClientTest {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void listenSub() throws Exception {
        RpcClient rpcClient = new RpcClient("ws://localhost:17711/orderdb");

        rpcClient.startup();

        Thread.sleep(2000);

        rpcClient.listenSub("sub.append",
                true,
                Lists.newArrayList("test"),
                new ChannelMessageHandler() {
                    @Override
                    public void handle(ChannelMessage channelMessage) {
                        logger.info("handle:{}", channelMessage.getResult());
                    }
                });

        logger.info("listenSub");

        int index = 10;
        while (index-- > 0) {
            rpcClient.callRpcAsync(
                    "append",
                    Lists.newArrayList("test", "key_4_" + index, Maps.newHashMap()));
        }

        logger.info("append end");

        Thread.sleep(300000);
    }

    @Test
    public void callRpcAsync() throws Exception {

        RpcClient rpcClient = new RpcClient("ws://localhost:17711/orderdb");

        rpcClient.startup();

        Thread.sleep(2000);

        StopWatch stopWatch = new StopWatch("callRpcAsync");

        stopWatch.start("get.latest.index");
        Future<ResponseObject> resFuture = rpcClient.callRpcAsync(
                "get.latest.index",
                Lists.newArrayList("test"),false);
        ResponseObject resObj = resFuture.get();

        logger.info("get.latest.index:{}", resObj.getResult());

        stopWatch.stop();

        stopWatch.start("append");
        int index = 50000;
        while (index-- > 0) {
            rpcClient.callRpcAsync(
                    "append",
                    Lists.newArrayList("test", "key_11_" + index, Maps.newHashMap()));
        }
        stopWatch.stop();

        stopWatch.start("get.latest.index");
        Future<ResponseObject> resFuture2 =
                rpcClient.callRpcAsync(
                        "get.latest.index",
                        Lists.newArrayList("test"),false);
        ResponseObject resObj2 = resFuture2.get();

        Future<ResponseObject> resFuture3 =
                rpcClient.callRpcAsync(
                        "get.latest.index",
                        Lists.newArrayList("test"),false);
        ResponseObject resObj3 = resFuture3.get();

        stopWatch.stop();

        stopWatch.start("sys.state");

        Future<ResponseObject> resFuture4 =
                rpcClient.callRpcAsync(
                        "sys.state",
                        Lists.newArrayList("test"),false);
        ResponseObject resObj4 = resFuture4.get();
        stopWatch.stop();

        logger.info("callRpcAsync:cost time:{}ms, {}", stopWatch.prettyPrint(), resObj4);
    }

}