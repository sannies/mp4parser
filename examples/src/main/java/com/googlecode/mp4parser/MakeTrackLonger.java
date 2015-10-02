package com.googlecode.mp4parser;

import org.mp4parser.Container;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.WrappingTrack;
import org.mp4parser.muxer.builder.DefaultMp4Builder;
import org.mp4parser.muxer.container.mp4.MovieCreator;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by sannies on 30.11.2014.
 */
public class MakeTrackLonger {
    public static void main(String[] args) throws IOException {
        Movie movieIn = MovieCreator.build(GetDuration.class.getProtectionDomain().getCodeSource().getLocation().getFile() + "/1365070268951.mp4");
        Movie movieOut = new Movie();
        for (Track track : movieIn.getTracks()) {
            if ("vide".equals(track.getHandler())) {
                movieOut.addTrack(new WrappingTrack(track) {
                    @Override
                    public long[] getSampleDurations() {
                        long[] l = super.getSampleDurations();
                        l[0] *= 10;
                        l[l.length-1] *= 10;
                        return l;
                    }
                });
            } else {
                movieOut.addTrack(track);
            }
        }
        DefaultMp4Builder defaultMp4Builder = new DefaultMp4Builder();
        Container mOut = defaultMp4Builder.build(movieOut);
        mOut.writeContainer(new FileOutputStream("default.mp4").getChannel());

    }
}
