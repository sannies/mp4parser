package com.googlecode.mp4parser;

import com.mp4parser.boxes.sampleentry.VisualSampleEntry;
import com.mp4parser.muxer.Movie;
import com.mp4parser.muxer.Track;
import com.mp4parser.muxer.container.mp4.MovieCreator;

import java.io.IOException;
import java.util.*;


public class FindCommonSync {

    public static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
        List<T> list = new ArrayList<T>(c);
        java.util.Collections.sort(list);
        return list;
    }

    public static void main(String[] args) throws IOException {
        HashMap<Integer, Integer> common = new HashMap<Integer, Integer>();
        boolean first = true;
        for (String arg : args) {
            Movie invideo = MovieCreator.build(arg);
            List<Track> tracks = invideo.getTracks();

            for (Track t : tracks) {
                String type = t.getSampleDescriptionBox().getSampleEntry().getType();
                System.out.println("Track of type " + type );
                if (t.getSampleDescriptionBox().getSampleEntry() instanceof VisualSampleEntry) {
                    HashMap<Integer, Integer> previous = (HashMap<Integer, Integer>) common.clone();
                    common.clear();
                    System.out.println("Found video track in " + arg);
                    long[] syncSamples = t.getSyncSamples();
                    long timescale = t.getTrackMetaData().getTimescale();
                    long tts = t.getSampleDurations()[0];
                    for (long syncSample : syncSamples) {
                        long time = 1000 * tts * (syncSample - 1) / timescale;
                        int inttime = (int) time;
                        if (first || previous.containsKey(inttime)) {
                            common.put(inttime, 1);
                        }
                    }
                    first = false;
                }
            }
        }
        // Print the common times
        Set<Integer> keys = common.keySet();

        List<Integer> inorder = asSortedList(keys);
        Integer previous = 0;
        int wrong = 0;
        for (Integer sync : inorder) {
            Integer delta = sync - previous;
            System.out.println("Common sync point: " + (double) sync / 1000.0 + " delta: " + (double) delta / 1000.0);
            if (delta > 3000) {
                System.out.println("WARNING WARNING! > 3sek");
                wrong++;
            }
            previous = sync;
        }
        int commonCount = inorder.size();
        System.out.println("Durations that are too long: " + wrong + "/" + commonCount);

    }
}
