package com.googlecode.mp4parser.muxformats;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.DataSource;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.tracks.AACTrackImpl;
import com.googlecode.mp4parser.authoring.tracks.h264.H264TrackImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * user: sannies
 */
public class BillH264Example {
    public static void main(String[] args) throws IOException {
        DataSource video_file = new FileDataSourceImpl("c:/dev/mp4parser2/source_video.h264");
        DataSource audio_file = new FileDataSourceImpl("c:/dev/mp4parser2/source_audio.aac");
        int duration = 30472;
        H264TrackImpl h264Track = new H264TrackImpl(video_file, "eng", 15000, 1001); //supplied duration for the attached file was
        AACTrackImpl aacTrack = new AACTrackImpl(audio_file);
        Movie movie = new Movie();
        movie.addTrack(h264Track);
        //movie.addTrack(aacTrack);

        Container out = new DefaultMp4Builder().build(movie);
        FileOutputStream fos = new FileOutputStream(new File("c:/dev/mp4parser2/checkme.mp4"));
        out.writeContainer(fos.getChannel());
        fos.close();

    }
}