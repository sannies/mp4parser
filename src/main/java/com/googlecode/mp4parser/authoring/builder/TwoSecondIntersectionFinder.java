package com.googlecode.mp4parser.authoring.builder;

import com.coremedia.iso.boxes.TimeToSampleBox;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;

import java.util.Arrays;
import java.util.List;

public class TwoSecondIntersectionFinder implements FragmentIntersectionFinder {

    protected long getDuration(Track track) {
        long duration = 0;
        for (TimeToSampleBox.Entry entry : track.getDecodingTimeEntries()) {
            duration += entry.getCount() * entry.getDelta();
        }
        return duration;
    }


    public int[] sampleNumbers(Track track, Movie movie) {
        List<TimeToSampleBox.Entry> entries = track.getDecodingTimeEntries();

        double trackLength = getDuration(track) / track.getTrackMetaData().getTimescale();

        int fragments[] = new int[(int) Math.ceil(trackLength / 2) - 1];
        Arrays.fill(fragments, -1);
        fragments[0] = 0;

        long time = 0;
        int samples = 0;
        for (TimeToSampleBox.Entry entry : entries) {
            for (int i = 0; i < entry.getCount(); i++) {
                int currentFragment = (int) (time / track.getTrackMetaData().getTimescale() / 2) + 1;
                if (currentFragment >= fragments.length) {
                    break;
                }
                fragments[currentFragment] = samples++;
                time += entry.getDelta();
            }
        }
        int last = samples;
        // fill all -1 ones.
        for (int i = fragments.length - 1; i >= 0 ; i--) {
            if (fragments[i] == -1) {
                fragments[i] = last;
            }
            last = fragments[i];
        }
        return fragments;

    }

}
