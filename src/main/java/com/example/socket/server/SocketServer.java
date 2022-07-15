package com.example.socket.server;

import com.example.socket.vo.MsgInfo;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * program nettyTest
 * <p>
 * description
 *
 * @author wuqia
 * @date 2022-05-14 21:59
 **/
@Slf4j
public class SocketServer extends Thread {
    // 张大爷
    static String [] msgs = new String[] {
            "刚吃",
            "您这，嘛去？",
            "有空来家坐坐啊"
    };

    @Getter
    private Long time;

    private CountDownLatch countDownLatch;

    private Boolean run = Boolean.TRUE;

    private CountDownLatch serverStart;

    ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public SocketServer(CountDownLatch serverStart, CountDownLatch countDownLatch, String name) {
        this.countDownLatch = countDownLatch;
        super.setName(name);
        this.serverStart = serverStart;
    }

    @Override
    public void run () {

        // 对话次数
        LongAdder longAdder = new LongAdder();
        longAdder.increment();
        LongAdder ackNum = new LongAdder();
        ackNum.increment();
        try (
                // 创建服务端socket
                ServerSocket serverSocket = new ServerSocket(8088)
        ) {
            serverStart.countDown();
            //循环监听等待客户端的连接
            int i = 0;
            LinkedBlockingQueue<MsgInfo> linkedBlockingQueue = new LinkedBlockingQueue<>(3 * MsgInfo.count);
            Long startTime = System.currentTimeMillis();
            while (run) {
                // 监听客户端
                Socket socket = serverSocket.accept();
                log.error("当前客户端的IP：" + socket.getInetAddress().getHostAddress());
                // 要发送给服务器的信息
                BufferedOutputStream bw = new BufferedOutputStream(socket.getOutputStream());
                new Thread() {
                    @SneakyThrows
                    @Override
                    public void run() {
                        while (longAdder.intValue() < 3 * MsgInfo.count) {
                            MsgInfo msgInfo = linkedBlockingQueue.take();
                            longAdder.increment();
                            if (msgInfo.getReq() == 1) {
                                MsgInfo.sendMsg(msgInfo.getSessionNum(), bw, 1, msgs[0], "李");
                                MsgInfo.sendMsg(msgInfo.getSessionNum(), bw, 2, msgs[1], "李");
                            } else if (msgInfo.getReq() == 2) {
                                MsgInfo.sendMsg(msgInfo.getSessionNum(), bw, 3, msgs[2], "李");
                            }
                        }
                        time = System.currentTimeMillis() - startTime;
                        log.error("已发送完{}， 耗时：{}", longAdder.intValue(), time);
                        countDownLatch.countDown();
                        //socket.close();
                        //Thread.sleep(1000);
                    }
                }.start();
                new Thread() {
                    @SneakyThrows
                    @Override
                    public void run() {
                        // 从客户端接收的信息
                        BufferedInputStream br = new BufferedInputStream(socket.getInputStream());
                        // 要发送给客户端的信息
                        while (ackNum.intValue() < MsgInfo.count * 3) {
                            MsgInfo msgInfo = null;
                            msgInfo = new MsgInfo("李", "张", br);
                            msgInfo.callBackLog();
                            linkedBlockingQueue.put(msgInfo);
                            ackNum.increment();
                        }
                    }
                }.start();
            }
        } catch (Exception e) {
            log.error("线程监听异常", e);
            // TODO: handle exception
            e.printStackTrace();
        }
    }
}
