package com.example.netty.server;

import com.example.netty.server.handler.TestHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * program nettyTest
 * <p>
 * description nettyServer
 *
 * @author wuqia
 * @date 2022-05-14 20:42
 **/
@Slf4j
public class NettyServer {

    // 创建一组线性
    EventLoopGroup group = new NioEventLoopGroup();
    // 工作线程
    EventLoopGroup work = new NioEventLoopGroup();

    NettyServer() {
        try{
            // 初始化 Server
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(group, work);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.localAddress(new InetSocketAddress("localhost", 9999));

            // 设置收到数据后的处理的 Handler
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new TestHandler());
                }
            });
            // 绑定端口，开始提供服务
            ChannelFuture channelFuture = serverBootstrap.bind().sync();
            channelFuture.channel().closeFuture().sync();
        } catch(Exception e){
            e.printStackTrace();
        } finally {
            try {
                group.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
