package org.mp4parser.streaming.input.aac;

import org.junit.Test;
import org.mp4parser.IsoFile;
import org.mp4parser.muxer.InMemRandomAccessSourceImpl;
import org.mp4parser.muxer.Sample;
import org.mp4parser.muxer.samples.SampleList;
import org.mp4parser.streaming.StreamingTrack;
import org.mp4parser.streaming.input.h264.Walk;
import org.mp4parser.streaming.output.mp4.FragmentedMp4Writer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.nio.channels.Channels;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by sannies on 02.09.2015.
 */
public class AdtsAacStreamingTrackTest {
    ExecutorService es = Executors.newCachedThreadPool();

    @Test
    public void testMuxing() throws Exception {
        AdtsAacStreamingTrack b = new AdtsAacStreamingTrack(AdtsAacStreamingTrackTest.class.getResourceAsStream("/org/mp4parser/streaming/input/aac/somesound.aac"), 65000, 80000);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new FragmentedMp4Writer(Collections.<StreamingTrack>singletonList(b), Channels.newChannel(baos));
        //MultiTrackFragmentedMp4Writer writer = new MultiTrackFragmentedMp4Writer(new StreamingTrack[]{b}, new ByteArrayOutputStream());
        b.call();
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