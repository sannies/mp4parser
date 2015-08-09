package com;

import com.mp4parser.Box;
import com.mp4parser.Container;
import com.mp4parser.muxer.Movie;
import com.mp4parser.muxer.builder.DefaultMp4Builder;
import com.mp4parser.muxer.container.mp4.MovieCreator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

public class Repair {
    public static void main(String[] args) throws IOException {
        Movie m = MovieCreator.build("C:\\dev\\drmtoday_cloud_encoder\\live.mp4");
        DefaultMp4Builder defaultMp4Builder = new DefaultMp4Builder();
        Container c = defaultMp4Builder.build(m);
        FileOutputStream fos = new FileOutputStream("C:\\dev\\drmtoday_cloud_encoder\\live-repaired.mp4");
        WritableByteChannel wbc = Channels.newChannel(fos);
        for (Box box : c.getBoxes()) {
            box.getBox(wbc);
        }
    }
}
