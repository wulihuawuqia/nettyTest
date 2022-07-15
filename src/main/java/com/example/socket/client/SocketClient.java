package com.example.socket.client;

import com.example.socket.vo.MsgInfo;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReadWriteLock;
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
public class SocketClient extends Thread {

    static String [] msgs = new String[] {
            "吃了没，您吶？",
            "嗨，没事儿溜溜弯儿。",
            "回头去给老太太请安！"
    };


    @Getter
    private Long time;

    private CountDownLatch countDownLatch;

    ReadWriteLock readWriteLock = new ReentrantReadWriteLock();


    public SocketClient(CountDownLatch countDownLatch, String name) {
        this.countDownLatch = countDownLatch;
        super.setName(name);
    }

    @Override
    public void run () {
        // 对话次数
        int count = MsgInfo.count;
        LongAdder ackNum = new LongAdder();
        ackNum.increment();
        LongAdder callBackNum = new LongAdder();
        callBackNum.increment();
        try (
                // 写消息
                Socket socketWrite = new Socket("localhost", 8088);
                BufferedOutputStream bw = new BufferedOutputStream(socketWrite.getOutputStream());
                // 从服务器接收的信息
                BufferedInputStream br = new BufferedInputStream(socketWrite.getInputStream())
        ) {
            // 发送第一条消息
            // |sessionNum|序号|长度|消息体|
            // 单独开线程发送请求
            Long startTime = System.currentTimeMillis();
            new Thread() {
                @SneakyThrows
                @Override
                public void run() {
                    for (int i = 1; i <= count; i++) {
                        MsgInfo.sendMsg(i, bw, 1, msgs[0], "张");
                        ackNum.increment();
                    }
                }
            }.start();

            //socket.shutdownOutput();

            String info = null;
            byte[] intByte = new byte[4];
            // 本次接收到的session
            int sessionNumCur = 1;
            int sessionNum = count * 3;
            LinkedBlockingQueue<MsgInfo> linkedBlockingQueue = new LinkedBlockingQueue<>(sessionNum);
            new Thread(){
                @SneakyThrows
                @Override
                public void run() {
                    while (ackNum.intValue() < count * 3) {
                        MsgInfo msgInfo = linkedBlockingQueue.take();
                        if (msgInfo.getReq() > 1 ) {
                            MsgInfo.sendMsg(msgInfo.getSessionNum(), bw, msgInfo.getReq(), msgs[msgInfo.getReq() - 1], "张");
                            ackNum.increment();
                        }
                    }

                }
            }.start();
            while(sessionNumCur <= count * 3) {
                MsgInfo msgInfo = new MsgInfo("张","李",br);
                msgInfo.callBackLog();
                callBackNum.increment();
                linkedBlockingQueue.put(msgInfo);
                // 判断是否需要终止
                if (callBackNum.intValue() == 3 * MsgInfo.count) {
                    time = System.currentTimeMillis() - startTime;
                    log.error("已接收消息{}， 耗时：{}", callBackNum.intValue(), time);
                    break;
                }
            }
            log.error("消息发送完成？");
            //socketWrite.close();
            countDownLatch.countDown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
