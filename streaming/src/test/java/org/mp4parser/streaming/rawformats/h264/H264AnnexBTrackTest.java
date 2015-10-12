package org.mp4parser.streaming.rawformats.h264;

import org.junit.Test;
import org.mp4parser.streaming.MultiTrackFragmentedMp4Writer;
import org.mp4parser.streaming.StreamingTrack;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by sannies on 16.08.2015.
 */
public class H264AnnexBTrackTest {
    ExecutorService es = Executors.newCachedThreadPool();


    @Test
    public void testMuxing() throws Exception {
        //H264AnnexBTrack b = new H264AnnexBTrack(H264AnnexBTrackTest.class.getResourceAsStream("/org/mp4parser/streaming/rawformats/h264/tos.h264"));
        H264AnnexBTrack b = new H264AnnexBTrack(new FileInputStream("C:\\dev\\mp4parser\\out.264"));
        OutputStream baos = new FileOutputStream("output.mp4");
        MultiTrackFragmentedMp4Writer writer = new MultiTrackFragmentedMp4Writer(Collections.<StreamingTrack>singletonList(b), baos);
        //MultiTrackFragmentedMp4Writer writer = new MultiTrackFragmentedMp4Writer(new StreamingTrack[]{b}, new ByteArrayOutputStream());
        b.call();
        writer.close();

        //Walk.through(isoFile);
        //List<Sample> s = new SampleList(1, isoFile, new InMemRandomAccessSourceImpl(baos.toByteArray()));
        //for (Sample sample : s) {
//            System.err.println("s: " + sample.getSize());
        //          sample.asByteBuffer();
        //    }
    }
}