package com.mp4parser.rtp2dash;

import com.mp4parser.streaming.StreamingTrack;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Server {

    private static final Logger LOG = Logger.getLogger(Server.class.getName());

    private int port;

    public Server(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        RtpH264StreamingTrack st = new RtpH264StreamingTrack("Z2QAFUs2QCAb5/ARAAADAAEAAAMAMI8WLZY=,aEquJyw=", 5000);
        ExecutorService es = Executors.newFixedThreadPool(2);
        Future<Void> f1 = es.submit(st);
        final File baseDir = new File("c:\\dev\\mp4parser\\out");
        final DashFragmentedMp4Writer dashFragmentedMp4Writer = new DashFragmentedMp4Writer(st, baseDir, 12, "rep", new ByteArrayOutputStream());
        Future<Void> f2 = es.submit(new Callable<Void>() {
            public Void call() throws Exception {
                try {
                    dashFragmentedMp4Writer.write();
                } catch (IOException e) {
                    LOG.severe(e.getLocalizedMessage());
                    throw e;
                }
                return null;
            }
        });
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        try {
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast("encoder", new HttpResponseEncoder());
                            p.addLast("decoder", new HttpRequestDecoder());
                            p.addLast("aggregator", new HttpObjectAggregator(65536));
                            GregorianCalendar gcNow = GregorianCalendar.from(ZonedDateTime.now());
                            gcNow.setTimeZone(TimeZone.getTimeZone("GMT"));
                            p.addLast("handler", new DashServerHandler(baseDir, gcNow, Collections.singletonList(dashFragmentedMp4Writer)));
                        }
                    });
            Channel ch = b.bind(port).sync().channel();

            f1.get();

            f2.get();

            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }


    public static void main(String[] args) throws Exception {
        LogManager.getLogManager().readConfiguration(Server.class.getResourceAsStream("/log.properties"));
        new Server(8080).run();
    }
}
