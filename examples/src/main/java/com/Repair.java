package com;

import org.mp4parser.Box;
import org.mp4parser.Container;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.builder.DefaultMp4Builder;
import org.mp4parser.muxer.container.mp4.MovieCreator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

public class Repair {
    public static void main(String[] args) throws IOException {
        Movie m = MovieCreator.build("c:\\content\\tears-of-steel-1080p-4min-handbraked.mp4");
        DefaultMp4Builder defaultMp4Builder = new DefaultMp4Builder();


        Container c = defaultMp4Builder.build(m);
        FileOutputStream fos = new FileOutputStream("C:\\content\\out.mp4");
        WritableByteChannel wbc = Channels.newChannel(fos);
        for (Box box : c.getBoxes()) {
            box.getBox(wbc);
        }
    }
}
