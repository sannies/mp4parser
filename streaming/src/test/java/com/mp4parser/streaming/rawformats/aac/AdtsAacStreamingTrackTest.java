package com.mp4parser.streaming.rawformats.aac;

import com.mp4parser.IsoFile;
import com.mp4parser.muxer.FileRandomAccessSourceImpl;
import com.mp4parser.muxer.InMemRandomAccessSourceImpl;
import com.mp4parser.muxer.Sample;
import com.mp4parser.muxer.samples.SampleList;
import com.mp4parser.streaming.MultiTrackFragmentedMp4Writer;
import com.mp4parser.streaming.StreamingTrack;
import com.mp4parser.streaming.rawformats.h264.H264AnnexBTrack;
import com.mp4parser.streaming.rawformats.h264.Walk;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Created by sannies on 02.09.2015.
 */
public class AdtsAacStreamingTrackTest {
    ExecutorService es = Executors.newCachedThreadPool();

    @Test
    public void testMuxing() throws Exception {
        AdtsAacStreamingTrack b = new AdtsAacStreamingTrack(AdtsAacStreamingTrackTest.class.getResourceAsStream("/com/mp4parser/streaming/rawformats/aac/somesound.aac"), 65000, 80000);
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
        new FileOutputStream("output.mp4").write(baos.toByteArray());
        Walk.through(isoFile);
        List<Sample> s = new SampleList(1, isoFile, new InMemRandomAccessSourceImpl(baos.toByteArray()));
        for (Sample sample : s) {
            //System.err.println("s: " + sample.getSize());
            sample.asByteBuffer();
        }


    }
}