package com.googlecode.mp4parser.muxformats;

import org.mp4parser.Container;
import org.mp4parser.muxer.FileDataSourceImpl;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.builder.DefaultMp4Builder;
import org.mp4parser.muxer.tracks.EC3TrackImpl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 *
 */
public class Ec3Example {
    public static void main(String[] args) throws IOException {
        EC3TrackImpl track = new EC3TrackImpl(new FileDataSourceImpl("C:\\Users\\sannies\\Downloads\\audio.ac3"));
        Movie m = new Movie();
        m.addTrack(track);
        DefaultMp4Builder mp4Builder = new DefaultMp4Builder();
        Container isoFile = mp4Builder.build(m);
        FileOutputStream fos = new FileOutputStream("output.mp4");
        FileChannel fc = fos.getChannel();
        isoFile.writeContainer(fc);
        fos.close();
    }
}
