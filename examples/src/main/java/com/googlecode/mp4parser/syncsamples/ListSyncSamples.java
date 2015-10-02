package com.googlecode.mp4parser.syncsamples;

import com.coremedia.iso.boxes.MovieBox;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class ListSyncSamples {

    public static void main(String[] args) throws IOException {
        Map<String, long[]> ss = new LinkedHashMap<String, long[]>();
        int maxIndex = 0;
        for (String arg : args) {
            File f = new File(arg);
            Movie m = MovieCreator.build(new FileDataSourceImpl(f));
            for (Track track : m.getTracks()) {
                if ("vide".equals(track.getHandler())) {
                    ss.put(f.getName() + track.getTrackMetaData().getTrackId(), track.getSyncSamples());
                    maxIndex = Math.max(maxIndex, track.getSyncSamples().length);
                }
            }
        }
        for (String s : ss.keySet()) {
            System.out.print(String.format("|%10s", s));
        }
        System.out.println("|");

        for (int i = 0; i < maxIndex; i++) {
            for (String s : ss.keySet()) {
                long[] syncSamples = ss.get(s);
                try {
                    System.out.print(String.format("|%10d", syncSamples[i]));
                } catch (IndexOutOfBoundsException e) {
                    System.out.print(String.format("|%10s", ""));
                }
            }
            System.out.println("|");
        }

    }
}
