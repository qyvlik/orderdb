package io.github.qyvlik.orderdb.modules.rpcclient;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.qyvlik.jsonrpclite.core.client.ChannelMessageHandler;
import io.github.qyvlik.jsonrpclite.core.client.RpcClient;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.response.ResponseObject;
import io.github.qyvlik.jsonrpclite.core.jsonsub.pub.ChannelMessage;
import io.github.qyvlik.orderdb.entity.request.AppendRequest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RpcClientTest {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void listenSub() throws Exception {
        RpcClient rpcClient = new RpcClient("ws://localhost:17711/orderdb", 1000, 10000);

        rpcClient.startup().get();          // sync

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

        RpcClient writeClient = new RpcClient("ws://localhost:17711/orderdb", 500, 20000);
        writeClient.startup().get();        // sync

        RpcClient readClient = new RpcClient("ws://localhost:17711/orderdb", 1000, 10000);
        readClient.startup().get();         // sync

        StopWatch stopWatch = new StopWatch("callRpcAsync");

        stopWatch.start("get.latest.index");
        Future<ResponseObject> resFuture = readClient.callRpc(
                "get.latest.index",
                Lists.newArrayList("test"));
        ResponseObject resObj = resFuture.get();

        logger.info("get.latest.index:{}", resObj.getResult());

        stopWatch.stop();

        stopWatch.start("append");
        int index = 15000;
        while (index-- > 0) {
            writeClient.callRpcAsync(
                    "append",
                    Lists.newArrayList("test", "key_25_" + index, Maps.newHashMap()));
        }
        stopWatch.stop();

        logger.info("append end");

        stopWatch.start("sys.state");

        int count = 0;
        boolean ready = false;
        while (!ready) {
            Future<ResponseObject> resFuture4 =
                    readClient.callRpc(
                            "sys.state",
                            Lists.newArrayList("test"));
            ResponseObject resObj4 = resFuture4.get();
            count++;
            if (resObj4.getResult().toString().equalsIgnoreCase("scope: test have pending size:0")) {
                ready = true;
            }
        }

        stopWatch.stop();


        stopWatch.start("get.latest.index");
        Future<ResponseObject> resFuture3 =
                readClient.callRpc(
                        "get.latest.index",
                        Lists.newArrayList("test"));
        ResponseObject resObj3 = resFuture3.get();
        logger.info("get.latest.index:{}", resObj3.getResult());
        stopWatch.stop();

        logger.info("callRpcAsync:requestCount:{}, {}", count, stopWatch.prettyPrint());
    }

    @Test
    public void testAppendList() throws Exception {
        StopWatch stopWatch = new StopWatch("testAppendList");

        RpcClient writeClient = new RpcClient("ws://localhost:17711/orderdb", 500, 2000000);

        stopWatch.start("connect");
        boolean connectResult = writeClient.startup().get();        // sync
        logger.info("connectResult:{}", connectResult);
        stopWatch.stop();

        stopWatch.start("active");
        testAppendList(writeClient, 1);         // active
        stopWatch.stop();

        int count = 10000;
        while (count-- > 0) {
            stopWatch.start("testAppendList:" + count);
            testAppendList(writeClient, 1);
            stopWatch.stop();
        }

        logger.info("testAppendList:{}", stopWatch.prettyPrint());
    }

    private void testAppendList(RpcClient writeClient, int totalCount) throws Exception {
        String scope = "test";
        int index = totalCount;
        List<AppendRequest> list = Lists.newLinkedList();
        while (index-- > 0) {
            Map<String, String> data = Maps.newHashMap();
            data.put("1", uuid());
            data.put("2", uuid());
            data.put("3", uuid());
            data.put("4", uuid());
            data.put("5", uuid());
            data.put("6", uuid());
            data.put("7", uuid());
            data.put("8", uuid());
            data.put("9", uuid());
            list.add(new AppendRequest(scope, uuid(), data));
        }

        Future<ResponseObject> resFuture1 =
                writeClient.callRpc(
                        "append.list",
                        Lists.newArrayList(scope, true, list));
        ResponseObject resObj1 = resFuture1.get();
        // logger.debug("testAppendList append.list:{}", ((List) resObj1.getResult()).size());
    }

    private String uuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }


    @Test
    public void testAppendListMultiThread() throws Exception {
        StopWatch stopWatch = new StopWatch("testAppendList");

        RpcClient writeClient = new RpcClient("ws://localhost:17711/orderdb", 500, 2000000);

        Executor executor = Executors.newFixedThreadPool(32);

        stopWatch.start("connect");
        boolean connectResult = writeClient.startup().get();        // sync
        logger.info("connectResult:{}", connectResult);
        stopWatch.stop();

        stopWatch.start("active");
        testAppendList(writeClient, 1);         // active
        stopWatch.stop();

        stopWatch.start("submit task");
        int count = 2000;
        BatchRunner batchRunner = new BatchRunner(executor, count);
        while (count-- > 0) {
            batchRunner.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        testAppendList(writeClient, 1);
                    } catch (Exception e) {
                    }
                }
            });
        }
        stopWatch.stop();

        stopWatch.start("waitDone");
        batchRunner.waitDone();
        stopWatch.stop();

        logger.info("testAppendList:{}", stopWatch.prettyPrint());
    }

}