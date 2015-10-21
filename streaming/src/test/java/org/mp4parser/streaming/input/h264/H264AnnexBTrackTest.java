package org.mp4parser.streaming.input.h264;

import org.junit.Test;
import org.mp4parser.IsoFile;
import org.mp4parser.muxer.InMemRandomAccessSourceImpl;
import org.mp4parser.muxer.Sample;
import org.mp4parser.muxer.container.mp4.MovieCreator;
import org.mp4parser.muxer.samples.SampleList;
import org.mp4parser.streaming.StreamingTrack;
import org.mp4parser.streaming.output.mp4.FragmentedMp4Writer;

import java.io.*;
import java.nio.channels.Channels;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by sannies on 16.08.2015.
 */
public class H264AnnexBTrackTest {
    ExecutorService es = Executors.newCachedThreadPool();


    @Test
    public void testMuxing() throws Exception {
        H264AnnexBTrack b = new H264AnnexBTrack(H264AnnexBTrackTest.class.getResourceAsStream("/org/mp4parser/streaming/input/h264/tos.h264"));
        //H264AnnexBTrack b = new H264AnnexBTrack(new FileInputStream("C:\\dev\\mp4parser\\out.264"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FragmentedMp4Writer writer = new FragmentedMp4Writer(Collections.<StreamingTrack>singletonList(b), Channels.newChannel(baos));
        //MultiTrackFragmentedMp4Writer writer = new MultiTrackFragmentedMp4Writer(new StreamingTrack[]{b}, new ByteArrayOutputStream());
        b.call();
        writer.close();
        IsoFile isoFile = new IsoFile(Channels.newChannel(new ByteArrayInputStream(baos.toByteArray())));
        Walk.through(isoFile);
        List<Sample> s = new SampleList(1, isoFile, new InMemRandomAccessSourceImpl(baos.toByteArray()));
        for (Sample sample : s) {
//            System.err.println("s: " + sample.getSize());
            sample.asByteBuffer();
        }
    }
}