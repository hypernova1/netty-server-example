package org.example.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class WebServer {

    public static void main(String[] args) {
        EventLoopGroup workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                    .option(ChannelOption.SO_BACKLOG, 500) // 동시에 수용 가능한 소켓 연결 요청 수
                    .childOption(ChannelOption.TCP_NODELAY, true) // 반응 쇽도를 높이기 위해 Nagle 알고리즘 비활성화
                    .childOption(ChannelOption.SO_LINGER, 0) // 소켓이 close될 때 신뢰성 있는 종료를 위해 4way-handshake 발생 후 TIME_WAIT가 발생해서 리소스가 낭비됨 이를 방지하기 위한 설정
                    .childOption(ChannelOption.SO_KEEPALIVE, true) // Keep-alive on
                    .childOption(ChannelOption.SO_REUSEADDR, true)
                    .childHandler(new ChannelInitializer<>() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            pipeline.addLast(new WebServerHandler());
                        }
                    });
            ChannelFuture future = bootstrap.bind(8080).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully().awaitUninterruptibly();
            workerGroup.shutdownGracefully().awaitUninterruptibly();
        }
    }

}
