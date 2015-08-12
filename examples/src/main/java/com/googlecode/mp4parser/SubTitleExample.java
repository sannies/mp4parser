package com.googlecode.mp4parser;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.TextTrackImpl;
import com.googlecode.mp4parser.authoring.tracks.webvtt.WebVttTrack;
import com.googlecode.mp4parser.srt.SrtParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Locale;

/**
 * Adds subtitles.
 */
public class SubTitleExample {
    public static void main(String[] args) throws IOException {
        Movie m = new Movie();
        String bd = "C:\\dev\\DRMTODAY-872\\";

        Track eng = MovieCreator.build(bd  + "31245689abb7c52a3d0721447bddd6cd_Tears_Of_Steel_128000_eng.mp4").getTracks().get(0);
        m.addTrack(eng);

        Track vid = MovieCreator.build(bd  + "31245689abb7c52a3d0721447bddd6cd_Tears_Of_Steel_600000.mp4").getTracks().get(0);
        m.addTrack(vid);

        Track sub = new WebVttTrack(new FileInputStream(bd  + "31245689abb7c52a3d0721447bddd6cd_Tears_Of_Steel_deu.vtt"), "subs", Locale.GERMAN);
        m.addTrack(sub);

        Container c = new DefaultMp4Builder().build(m);

        c.writeContainer(new FileOutputStream("output.mp4").getChannel());
    }

}
