package org.mp4parser.examples.mux.filebased;

import org.mp4parser.Container;
import org.mp4parser.muxer.FileDataSourceImpl;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.builder.DefaultFragmenterImpl;
import org.mp4parser.muxer.builder.DefaultMp4Builder;
import org.mp4parser.muxer.builder.FragmentedMp4Builder;
import org.mp4parser.muxer.builder.Mp4Builder;
import org.mp4parser.muxer.tracks.h264.H264TrackImpl;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by sannies on 26.10.2015.
 */
public class MuxMe {
    public static void main(String[] args) throws IOException {
        H264TrackImpl h264Track = new H264TrackImpl(new FileDataSourceImpl("/Users/sannies/dev/mp4parser/1532667754113.h264"));
        Movie m = new Movie();
        m.addTrack(h264Track);
        Mp4Builder builder = new DefaultMp4Builder();

        Container c = builder.build(m);
        c.writeContainer(new FileOutputStream("/Users/sannies/dev/mp4parser/1532667754113-meins.mp4").getChannel());
    }
}
