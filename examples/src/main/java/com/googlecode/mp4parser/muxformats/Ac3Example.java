package com.googlecode.mp4parser.muxformats;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.tracks.AC3TrackImpl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 *
 */
public class Ac3Example {
    public static void main(String[] args) throws IOException {
        Track ac3Track = new AC3TrackImpl(new FileDataSourceImpl("C:\\dev\\mp4parser\\examples\\src\\main\\resources\\count-english.ac3"));
        //Track ac3Track = new AC3TrackImplOld(new BufferedInputStream(new FileInputStream("C:\\dev\\mp4parser\\examples\\src\\main\\resources\\count-english.ac3")));
        Movie m = new Movie();
        m.addTrack(ac3Track);
        DefaultMp4Builder mp4Builder = new DefaultMp4Builder();
        Container out = mp4Builder.build(m);
        FileOutputStream fos = new FileOutputStream("output.mp4");
        FileChannel fc = fos.getChannel();
        out.writeContainer(fc);
        fos.close();
    }
}
