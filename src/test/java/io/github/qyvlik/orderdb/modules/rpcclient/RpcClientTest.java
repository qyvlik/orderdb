package io.github.qyvlik.orderdb.modules.rpcclient;

import com.google.common.collect.Lists;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.response.ResponseObject;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;

public class RpcClientTest {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void listenSub() throws Exception {
    }

    @Test
    public void callRpcAsync() throws Exception {

        RpcClient rpcClient = new RpcClient("ws://localhost:17711/orderdb");

        rpcClient.startup();

        Thread.sleep(2000);

//        int index = 10000;
//        while (index-- > 0) {
//            rpcClient.callRpcAsync(
//                    "append",
//                    Lists.newArrayList("test", "key" + index, Maps.newHashMap()),
//                    false);
//        }


        rpcClient.callRpcAsync(
                "get.latest.index",
                Lists.newArrayList("test"),
                false);


        long start = System.currentTimeMillis();

        Future<ResponseObject> resFuture =
                rpcClient.callRpcAsync(
                        "get.latest.index",
                        Lists.newArrayList("test"),
                        true);

        ResponseObject resObj = resFuture.get();

        long end = System.currentTimeMillis();


        logger.info("callRpcAsync:cost time:{}ms, {}", end - start, resObj);

    }

}