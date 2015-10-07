package com.googlecode.mp4parser;

import org.mp4parser.Container;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.builder.DefaultMp4Builder;
import org.mp4parser.muxer.container.mp4.MovieCreator;
import org.mp4parser.muxer.tracks.AppendTrack;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class AppendExample {
    public static void main(String[] args) throws IOException {


        String[] videoUris = new String[]{

                "c:\\content\\20150930_161515.mp4",
                "c:\\content\\20150930_161525.mp4",
                "c:\\content\\20150930_161529.mp4",
                "c:\\content\\20150930_161534.mp4",
                "c:\\content\\20150930_161543.mp4",
                "c:\\content\\20151001_135436.mp4",
                "c:\\content\\20151001_135446.mp4",
                "c:\\content\\20150930_161515.mp4",
                "c:\\content\\20150930_161525.mp4",
                "c:\\content\\20150930_161529.mp4",
                "c:\\content\\20150930_161534.mp4",
                "c:\\content\\20150930_161543.mp4",
                "c:\\content\\20151001_135436.mp4",
                "c:\\content\\20151001_135446.mp4",
                "c:\\content\\20150930_161515.mp4",
                "c:\\content\\20150930_161525.mp4",
                "c:\\content\\20150930_161529.mp4",
                "c:\\content\\20150930_161534.mp4",
                "c:\\content\\20150930_161543.mp4",
                "c:\\content\\20151001_135436.mp4",
                "c:\\content\\20151001_135446.mp4",
                "c:\\content\\20150930_161515.mp4",
                "c:\\content\\20150930_161525.mp4",
                "c:\\content\\20150930_161529.mp4",
                "c:\\content\\20150930_161534.mp4",
                "c:\\content\\20150930_161543.mp4",
                "c:\\content\\20151001_135436.mp4",
                "c:\\content\\20151001_135446.mp4",
                "c:\\content\\20150930_161515.mp4",
                "c:\\content\\20150930_161525.mp4",
                "c:\\content\\20150930_161529.mp4",
                "c:\\content\\20150930_161534.mp4",
                "c:\\content\\20150930_161543.mp4",
                "c:\\content\\20151001_135436.mp4",
                "c:\\content\\20151001_135446.mp4",
                "c:\\content\\20150930_161515.mp4",
                "c:\\content\\20150930_161525.mp4",
                "c:\\content\\20150930_161529.mp4",
                "c:\\content\\20150930_161534.mp4",
                "c:\\content\\20150930_161543.mp4",
                "c:\\content\\20151001_135436.mp4",
                "c:\\content\\20151001_135446.mp4",
                "c:\\content\\20150930_161515.mp4",
                "c:\\content\\20150930_161525.mp4",
                "c:\\content\\20150930_161529.mp4",
                "c:\\content\\20150930_161534.mp4",
                "c:\\content\\20150930_161543.mp4",
                "c:\\content\\20151001_135436.mp4",
                "c:\\content\\20151001_135446.mp4",
                "c:\\content\\20150930_161515.mp4",
                "c:\\content\\20150930_161525.mp4",
                "c:\\content\\20150930_161529.mp4",
                "c:\\content\\20150930_161534.mp4",
                "c:\\content\\20150930_161543.mp4",
                "c:\\content\\20151001_135436.mp4",
                "c:\\content\\20151001_135446.mp4",
                "c:\\content\\20150930_161515.mp4",
                "c:\\content\\20150930_161525.mp4",
                "c:\\content\\20150930_161529.mp4",
                "c:\\content\\20150930_161534.mp4",
                "c:\\content\\20150930_161543.mp4",
                "c:\\content\\20151001_135436.mp4",
                "c:\\content\\20151001_135446.mp4",
                "c:\\content\\20150930_161515.mp4",
                "c:\\content\\20150930_161525.mp4",
                "c:\\content\\20150930_161529.mp4",
                "c:\\content\\20150930_161534.mp4",
                "c:\\content\\20150930_161543.mp4",
                "c:\\content\\20151001_135436.mp4",
                "c:\\content\\20151001_135446.mp4",
                "c:\\content\\20150930_161515.mp4",
                "c:\\content\\20150930_161525.mp4",
                "c:\\content\\20150930_161529.mp4",
                "c:\\content\\20150930_161534.mp4",
                "c:\\content\\20150930_161543.mp4",
                "c:\\content\\20151001_135436.mp4",
                "c:\\content\\20151001_135446.mp4",
                "c:\\content\\20150930_161515.mp4",
                "c:\\content\\20150930_161525.mp4",
                "c:\\content\\20150930_161529.mp4",
                "c:\\content\\20150930_161534.mp4",
                "c:\\content\\20150930_161543.mp4",
                "c:\\content\\20151001_135436.mp4",
                "c:\\content\\20151001_135446.mp4",
                "c:\\content\\20151001_135540.mp4"

        };

        List<Movie> inMovies = new ArrayList<Movie>();
        for (String videoUri : videoUris) {
            inMovies.add(MovieCreator.build(videoUri));
        }

        List<Track> videoTracks = new LinkedList<Track>();
        List<Track> audioTracks = new LinkedList<Track>();

        for (Movie m : inMovies) {
            for (Track t : m.getTracks()) {
                if (t.getHandler().equals("soun")) {
                    audioTracks.add(t);
                }
                if (t.getHandler().equals("vide")) {
                    videoTracks.add(t);
                }
            }
        }

        Movie result = new Movie();

        if (audioTracks.size() > 0) {
            result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
        }
        if (videoTracks.size() > 0) {
            result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
        }

        Container out = new DefaultMp4Builder().build(result);

        FileChannel fc = new RandomAccessFile(String.format("output.mp4"), "rw").getChannel();
        out.writeContainer(fc);
        fc.close();


    }


}
