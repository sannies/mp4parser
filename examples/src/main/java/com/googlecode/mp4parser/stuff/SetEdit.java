package com.googlecode.mp4parser.stuff;

import com.googlecode.mp4parser.authoring.Edit;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by sannies on 28.01.2015.
 */
public class SetEdit {
    public static void main(String[] args) throws IOException {
        Movie m = MovieCreator.build("C:\\dev\\mp4parser\\examples\\src\\main\\resources\\1365070453555.mp4");
        for (Track track : m.getTracks()) {
            track.getEdits().clear();;
            track.getEdits().add(new Edit(0, 1, 1, 2.0));
        }
        new DefaultMp4Builder().build(m).writeContainer(new FileOutputStream("output.mp4").getChannel());
    }
}
