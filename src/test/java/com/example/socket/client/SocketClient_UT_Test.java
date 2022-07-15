package com.example.socket.client;


import com.example.socket.server.SocketServer;
import com.example.socket.vo.MsgInfo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;


@Slf4j
public class SocketClient_UT_Test {

    CountDownLatch countDownLatch = new CountDownLatch(2);
    CountDownLatch serverStart = new CountDownLatch(1);


    @SneakyThrows
    @Test
    public void beforeEach() {
        MsgInfo.count = 100000;
        SocketServer socketServerThread = new SocketServer(serverStart, countDownLatch, "李");
        SocketClient socketClientThread = new SocketClient(countDownLatch, "张");
        socketServerThread.start();
        serverStart.await();
        socketClientThread.start();
        countDownLatch.await();
        log.error("完成：{} 张消耗时间：{}, 李消耗时间{}", MsgInfo.count, socketClientThread.getTime(), socketServerThread.getTime());
    }

    @Test
    public void startSocketServer() {
        MsgInfo.count = 1000000;
        SocketServer socketServerThread = new SocketServer(serverStart, countDownLatch, "李");
        socketServerThread.run();
    }

    @Test
    public void startSocketClient() {
        MsgInfo.count = 1000000;
        SocketClient socketClientThread = new SocketClient(countDownLatch, "张");
        socketClientThread.run();
    }



}

