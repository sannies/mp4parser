package com.googlecode.mp4parser;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.TextTrackImpl;
import com.googlecode.mp4parser.srt.SrtParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Adds subtitles.
 */
public class SubTitleExample {
    public static void main(String[] args) throws IOException {
        String audioEnglish = RemoveSomeSamplesExample.class.getProtectionDomain().getCodeSource().getLocation().getFile() + "/count-video.mp4";
        Movie countVideo = MovieCreator.build(audioEnglish);


        TextTrackImpl subTitleEng = new TextTrackImpl();
        subTitleEng.getTrackMetaData().setLanguage("eng");


        subTitleEng.getSubs().add(new TextTrackImpl.Line(5000, 6000, "Five"));
        subTitleEng.getSubs().add(new TextTrackImpl.Line(8000, 9000, "Four"));
        subTitleEng.getSubs().add(new TextTrackImpl.Line(12000, 13000, "Three"));
        subTitleEng.getSubs().add(new TextTrackImpl.Line(16000, 17000, "Two"));
        subTitleEng.getSubs().add(new TextTrackImpl.Line(20000, 21000, "one"));

        countVideo.addTrack(subTitleEng);

        TextTrackImpl subTitleDeu = SrtParser.parse(SubTitleExample.class.getResourceAsStream("/count-subs-deutsch.srt"));
        subTitleDeu.getTrackMetaData().setLanguage("deu");
        countVideo.addTrack(subTitleDeu);

        Container out = new DefaultMp4Builder().build(countVideo);
        FileOutputStream fos = new FileOutputStream(new File("output.mp4"));
        FileChannel fc = fos.getChannel();
        out.writeContainer(fc);
        fos.close();
    }

}
