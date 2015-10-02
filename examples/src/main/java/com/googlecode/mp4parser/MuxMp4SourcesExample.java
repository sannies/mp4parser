package com.googlecode.mp4parser;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.builder.FragmentedMp4Builder;
import com.googlecode.mp4parser.authoring.builder.SyncSampleIntersectFinderImpl;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Muxes 2 audio tracks with a video track.
 */
public class MuxMp4SourcesExample {
    public static void main(String[] args) throws IOException {

        String audioDeutsch = MuxMp4SourcesExample.class.getProtectionDomain().getCodeSource().getLocation().getFile() + "/count-deutsch-audio.mp4";
        String audioEnglish = MuxMp4SourcesExample.class.getProtectionDomain().getCodeSource().getLocation().getFile() + "/count-english-audio.mp4";
        String video = MuxMp4SourcesExample.class.getProtectionDomain().getCodeSource().getLocation().getFile() + "/count-video.mp4";


        Movie countVideo = MovieCreator.build(video);
        Movie countAudioDeutsch = MovieCreator.build(audioDeutsch);
        Movie countAudioEnglish = MovieCreator.build(audioEnglish);

        Track audioTrackDeutsch = countAudioDeutsch.getTracks().get(0);
        audioTrackDeutsch.getTrackMetaData().setLanguage("deu");
        Track audioTrackEnglish = countAudioEnglish.getTracks().get(0);
        audioTrackEnglish.getTrackMetaData().setLanguage("eng");

        countVideo.addTrack(audioTrackDeutsch);
        countVideo.addTrack(audioTrackEnglish);

        {
            Container out = new DefaultMp4Builder().build(countVideo);
            FileOutputStream fos = new FileOutputStream(new File("output.mp4"));
            out.writeContainer(fos.getChannel());
            fos.close();
        }
        {
            FragmentedMp4Builder fragmentedMp4Builder = new FragmentedMp4Builder();
            fragmentedMp4Builder.setFragmenter(new SyncSampleIntersectFinderImpl(countVideo, null, -1));
            Container out = fragmentedMp4Builder.build(countVideo);
            FileOutputStream fos = new FileOutputStream(new File("output-frag.mp4"));
            out.writeContainer(fos.getChannel());
            fos.close();
        }
    }

}
