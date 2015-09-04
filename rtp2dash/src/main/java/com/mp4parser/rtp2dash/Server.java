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
import io.netty.util.concurrent.EventExecutor;

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
    ExecutorService es = Executors.newCachedThreadPool();

    private static final Logger LOG = Logger.getLogger(Server.class.getName());

    private int port;

    public Server(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        List<RtpH264StreamingTrack> h264s = new ArrayList<RtpH264StreamingTrack>();
        //final RtpH264StreamingTrack h264_0 = new RtpH264StreamingTrack("Z2QAFUs2QCAb5/ARAAADAAEAAAMAMI8WLZY=,aEquJyw=", 5000);
        final RtpH264StreamingTrack h264_1 = new RtpH264StreamingTrack("Z2QAFWs2QCcIebwEQAAAAwBAAAAMI8WLZYA=,aG6uJyw=", 5001);
        //final RtpH264StreamingTrack h264_2 = new RtpH264StreamingTrack("Z2QAHiLNkAwCmwEQAAADABAAAAMDCPFi2WA=,aCEq4nLA", 5002);
        //final RtpH264StreamingTrack h264_3 = new RtpH264StreamingTrack("Z2QAHyrNkASA9sBEAAADAAQAAAMAwjxgxlg=,aClq4nLA", 5003);
        final RtpAacStreamingTrack aac_eng = new RtpAacStreamingTrack(5004, 97, 128, "profile-level-id=1;mode=AAC-hbr;sizelength=13;indexlength=3;indexdeltalength=3; config=1190", "MPEG4-GENERIC/48000/2");
        aac_eng.setLanguage("eng");
        final RtpAacStreamingTrack aac_ita = new RtpAacStreamingTrack(5005, 97, 128, "profile-level-id=1;mode=AAC-hbr;sizelength=13;indexlength=3;indexdeltalength=3; config=1190", "MPEG4-GENERIC/48000/2");
        aac_ita.setLanguage("ita");
        List<Future<Void>> futures = new ArrayList<Future<Void>>();
        futures.add(es.submit(aac_ita));
        futures.add(es.submit(aac_eng));
        //futures.add(es.submit(h264_0));
        futures.add(es.submit(h264_1));
        //futures.add(es.submit(h264_2));
        //futures.add(es.submit(h264_3));

        final File baseDir = File.createTempFile("live", "server");
        baseDir.delete();
        baseDir.mkdir();

        final DashFragmentedMp4Writer aacItaWriter = new DashFragmentedMp4Writer(aac_ita, baseDir, 2, "aac_ita", new ByteArrayOutputStream());
        futures.add(es.submit(new WriterCallable(aacItaWriter)));
        final DashFragmentedMp4Writer aacEngWriter = new DashFragmentedMp4Writer(aac_eng, baseDir, 3, "aac_eng", new ByteArrayOutputStream());
        futures.add(es.submit(new WriterCallable(aacEngWriter)));

//        final DashFragmentedMp4Writer h264Q0Writer = new DashFragmentedMp4Writer(h264_0, baseDir, 1, "h264_0", new ByteArrayOutputStream());
//        futures.add(es.submit(new WriterCallable(h264Q0Writer)));
        final DashFragmentedMp4Writer h264Q1Writer = new DashFragmentedMp4Writer(h264_1, baseDir, 1, "h264_1", new ByteArrayOutputStream());
        futures.add(es.submit(new WriterCallable(h264Q1Writer)));
//        final DashFragmentedMp4Writer h264Q2Writer = new DashFragmentedMp4Writer(h264_2, baseDir, 1, "h264_2", new ByteArrayOutputStream());
//        futures.add(es.submit(new WriterCallable(h264Q2Writer)));
//        final DashFragmentedMp4Writer h264Q3Writer = new DashFragmentedMp4Writer(h264_3, baseDir, 1, "h264_3", new ByteArrayOutputStream());
//        futures.add(es.submit(new WriterCallable(h264Q3Writer)));

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
                            p.addLast("handler", new DashServerHandler(baseDir, gcNow, Arrays.asList(
                                    aacEngWriter,
                                    aacItaWriter,
//                                    h264Q0Writer,
                                    h264Q1Writer
//                                    h264Q2Writer,
//                                    h264Q3Writer
                            )));
                        }
                    });
            Channel ch = b.bind(port).sync().channel();

            for (Future<Void> future : futures) {
                future.get();
            }

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

    public static class WriterCallable implements Callable<Void> {
        private DashFragmentedMp4Writer writer;

        public WriterCallable(DashFragmentedMp4Writer writer) {
            this.writer = writer;
        }

        public Void call() throws Exception {
            writer.write();
            return null;
        }
    }
}
