package org.mp4parser.streaming.output.mp4;

import org.junit.Test;
import org.mp4parser.streaming.StreamingTrack;
import org.mp4parser.streaming.input.h264.H264AnnexBTrack;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.util.Collections;


public class FragmentedMp4WriterTest {

    @Test
    public void testMuxing() throws Exception {
        H264AnnexBTrack b = new H264AnnexBTrack(FragmentedMp4WriterTest.class.getResourceAsStream("/org/mp4parser/streaming/input/h264/tos.h264"));
        OutputStream baos = new FileOutputStream("output_fragmented.mp4");
        //StandardMp4Writer writer = new StandardMp4Writer(Collections.<StreamingTrack>singletonList(b), Channels.newChannel(baos));
        FragmentedMp4Writer writer = new FragmentedMp4Writer(Collections.<StreamingTrack>singletonList(b), Channels.newChannel(baos));
        b.call();
        writer.close();

        //Walk.through(isoFile);
        //List<Sample> s = new Mp4SampleList(1, isoFile, new InMemRandomAccessSourceImpl(baos.toByteArray()));
        //for (Sample sample : s) {
//            System.err.println("s: " + sample.getSize());
        //          sample.asByteBuffer();
        //    }
    }

}