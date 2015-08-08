package com.googlecode.mp4parser.muxformats;

import com.mp4parser.Container;
import com.mp4parser.authoring.FileDataSourceImpl;
import com.mp4parser.authoring.Movie;
import com.mp4parser.authoring.Track;
import com.mp4parser.authoring.builder.DefaultMp4Builder;
import com.mp4parser.authoring.tracks.DTSTrackImpl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by sannies on 2/12/14.
 */
public class DTSMuxExample {
    public static void main(String[] args) throws IOException {
        Movie movie = new Movie();
        Track track = new DTSTrackImpl(new FileDataSourceImpl("C:\\Users\\sannies\\Downloads\\Big_Dom_Thl_ENG_5.1_HD_Lossless_1510.dtshd"));
        movie.addTrack(track);

        DefaultMp4Builder builder = new DefaultMp4Builder();
        Container container = builder.build(movie);
        FileOutputStream fos = new FileOutputStream("c:\\dev\\isoparser-dtshd-test.mp4");
        FileChannel fc = fos.getChannel();
        container.writeContainer(fc);
        fos.close();
    }
}
