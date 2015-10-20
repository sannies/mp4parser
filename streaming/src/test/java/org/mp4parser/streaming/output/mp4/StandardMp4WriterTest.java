package org.mp4parser.streaming.output.mp4;

import org.junit.Test;
import org.mp4parser.streaming.StreamingTrack;
import org.mp4parser.streaming.input.h264.H264AnnexBTrack;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.util.Collections;

/**
 * Created by sannies on 20.10.2015.
 */
public class StandardMp4WriterTest {

    @Test
    public void testMuxing() throws Exception {
        H264AnnexBTrack b = new H264AnnexBTrack(new FileInputStream("C:\\dev\\mp4parser\\out.264"));
        OutputStream baos = new FileOutputStream("output.mp4");
        StandardMp4Writer writer = new StandardMp4Writer(Collections.<StreamingTrack>singletonList(b), Channels.newChannel(baos));
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