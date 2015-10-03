package com.googlecode.mp4parser.muxformats;

import org.mp4parser.Container;
import org.mp4parser.muxer.FileDataSourceImpl;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.builder.DefaultMp4Builder;
import org.mp4parser.muxer.tracks.AACTrackImpl;
import org.mp4parser.muxer.tracks.h264.H264TrackImpl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created with IntelliJ IDEA.
 * User: magnus
 * Date: 2012-04-20
 * Time: 13:22
 * To change this template use File | Settings | File Templates.
 */
public class AacExample {
    public static void main(String[] args) throws IOException {

        AACTrackImpl aacTrack = new AACTrackImpl(new FileDataSourceImpl("C:\\content\\Cosmos Laundromat small.aac"));
        H264TrackImpl h264Track = new H264TrackImpl(new FileDataSourceImpl("C:\\content\\Cosmos Laundromat small.264"));
        Movie m = new Movie();
        m.addTrack(aacTrack);
        m.addTrack(h264Track);
        DefaultMp4Builder mp4Builder = new DefaultMp4Builder();
        Container out = mp4Builder.build(m);
        FileOutputStream fos = new FileOutputStream("output.mp4");
        FileChannel fc = fos.getChannel();
        out.writeContainer(fc);

        fos.close();
    }
}
