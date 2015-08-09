package com.googlecode.mp4parser.stuff;

import com.mp4parser.Container;
import com.mp4parser.muxer.Movie;
import com.mp4parser.muxer.Track;
import com.mp4parser.muxer.builder.DefaultMp4Builder;
import com.mp4parser.muxer.container.mp4.MovieCreator;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by sannies on 03.10.2014.
 */
public class RemoveVideo {
    public static void main(String[] args) throws IOException {
        Movie mWithVideo = MovieCreator.build("C:\\dev\\mp4parser\\examples\\src\\main\\resources\\davidappend\\v1.mp4");
        Movie mWOutVideo = new Movie();
        for (Track track : mWithVideo.getTracks()) {
            if (track.getHandler().equals("soun")) {
                mWOutVideo.addTrack(track);
            }
        }
        DefaultMp4Builder b = new DefaultMp4Builder();
        Container c = b.build(mWOutVideo);
        c.writeContainer(new FileOutputStream("output.mp4").getChannel());
    }
}
