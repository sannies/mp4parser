package com.mp4parser.streaming.rawformats.h264;

import com.mp4parser.IsoFile;
import com.mp4parser.muxer.FileRandomAccessSourceImpl;
import com.mp4parser.muxer.Sample;
import com.mp4parser.muxer.samples.SampleList;
import com.mp4parser.streaming.MultiTrackFragmentedMp4Writer;
import com.mp4parser.streaming.StreamingTrack;
import org.junit.Test;

import java.io.*;
import java.nio.channels.Channels;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by sannies on 16.08.2015.
 */
public class H264AnnexBTrackTest {
    ExecutorService es = Executors.newCachedThreadPool();


    @Test
    public void testMuxing() throws Exception {
        H264AnnexBTrack b = new H264AnnexBTrack(H264AnnexBTrackTest.class.getResourceAsStream("/com/mp4parser/streaming/rawformats/h264/tos.h264"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MultiTrackFragmentedMp4Writer writer = new MultiTrackFragmentedMp4Writer(Collections.<StreamingTrack>singletonList(b), baos);
        //MultiTrackFragmentedMp4Writer writer = new MultiTrackFragmentedMp4Writer(new StreamingTrack[]{b}, new ByteArrayOutputStream());
        Future<Void> f = es.submit(b);
        writer.write();
        es.shutdown();
        es.awaitTermination(1, TimeUnit.MINUTES);
        try {
            f.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        IsoFile isoFile = new IsoFile(Channels.newChannel(new ByteArrayInputStream(baos.toByteArray())));
        Walk.through(isoFile);
        List<Sample> s = new SampleList(1, isoFile, new FileRandomAccessSourceImpl(new RandomAccessFile("output.mp4", "r")));
        for (Sample sample : s) {
            //System.err.println("s: " + sample.getSize());
            sample.asByteBuffer();
        }
    }
}