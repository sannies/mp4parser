package com;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

/**
 * Read Mp4 and write.
 */
public class Repair {
    public static void main(String[] args) throws IOException {
        Movie m = MovieCreator.build("c:\\content\\ffmpeg.mp4");
        DefaultMp4Builder defaultMp4Builder = new DefaultMp4Builder();
        Container c = defaultMp4Builder.build(m);
        FileOutputStream fos = new FileOutputStream("C:\\content\\out.mp4");
        WritableByteChannel wbc = Channels.newChannel(fos);
        c.writeContainer(wbc);
    }
}
