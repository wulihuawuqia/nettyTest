package com.example.socket.vo;

import cn.hutool.core.util.ByteUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * program nettyTest
 * <p>
 * description 消息
 *
 * @author wuqia
 * @date 2022-05-15 11:23
 **/
@Data
@Slf4j
public class MsgInfo {

    public static int count = 3;

    private int sessionNum;

    private int req;

    private int len;

    private String msg;

    private String name;

    private String callBackName;

    public MsgInfo(String name, String callBackName, BufferedInputStream br) throws IOException {
        //log.error("br available ={}", br.available());
        this.name = name;
        this.callBackName = callBackName;
        byte[] intByte = new byte[4];
        br.read(intByte);
        sessionNum = ByteUtil.bytesToInt(intByte);
        br.read(intByte);
        req = ByteUtil.bytesToInt(intByte);
        br.read(intByte);
        len = ByteUtil.bytesToInt(intByte);
        byte[] receiveMsgByte;
        try {
            receiveMsgByte = new byte[len];
        } catch (Exception e) {
            log.error("callBackLog sessionNum:{} req:{} len:{} {}:{}", sessionNum,req,len, callBackName, msg);
            throw new RuntimeException(e);
        }

        br.read(receiveMsgByte);
        msg = new String(receiveMsgByte, StandardCharsets.UTF_8);
    }

    public void callBackLog() {
        //log.error("callBackLog sessionNum:{} {}:{}", sessionNum, callBackName, msg);
    }

    public synchronized static void sendMsg(int sessionNum,
                               BufferedOutputStream bw,
                               int req,
                               String msg, String name) throws IOException {
        //log.error("sendMsg sessionNum:{} {}:{}", sessionNum, name, msg);
        byte[] sessionNumByte = ByteUtil.intToBytes(sessionNum);
        byte[] seqByte = ByteUtil.intToBytes(req);
        byte[] msgByte = msg.getBytes(StandardCharsets.UTF_8);
        byte[] lenByte = ByteUtil.intToBytes(msgByte.length);
        try {
            bw.write(sessionNumByte);
            bw.write(seqByte);
            bw.write(lenByte);
            bw.write(msgByte);
            bw.flush();
        } catch (IOException ioException) {
            log.error("sendMsg error", ioException);
            throw new RuntimeException(ioException);
        }

    }

}
