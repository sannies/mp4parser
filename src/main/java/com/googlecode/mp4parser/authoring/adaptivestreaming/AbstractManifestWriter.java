package com.googlecode.mp4parser.authoring.adaptivestreaming;

import com.coremedia.iso.boxes.OriginalFormatBox;
import com.coremedia.iso.boxes.sampleentry.AbstractSampleEntry;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Sample;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.FragmentIntersectionFinder;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

public abstract class AbstractManifestWriter implements ManifestWriter {
    private static final Logger LOG = Logger.getLogger(AbstractManifestWriter.class.getName());

    private FragmentIntersectionFinder intersectionFinder;
    protected long[] audioFragmentsDurations;
    protected long[] videoFragmentsDurations;

    protected AbstractManifestWriter(FragmentIntersectionFinder intersectionFinder) {
        this.intersectionFinder = intersectionFinder;
    }

    /**
     * Calculates the length of each fragment in the given <code>track</code> (as part of <code>movie</code>).
     *
     * @param track target of calculation
     * @param movie the <code>track</code> must be part of this <code>movie</code>
     * @return the duration of each fragment in track timescale
     */
    public long[] calculateFragmentDurations(Track track, Movie movie) {
        long[] startSamples = intersectionFinder.sampleNumbers(track);
        long[] durations = new long[startSamples.length];
        int currentFragment = 0;
        int currentSample = 1; // sync samples start with 1 !

        for (long delta : track.getSampleDurations()) {
            for (int max = currentSample + 1; currentSample < max; currentSample++) {
                // in this loop we go through the entry.getCount() samples starting from current sample.
                // the next entry.getCount() samples have the same decoding time.
                if (currentFragment != startSamples.length - 1 && currentSample == startSamples[currentFragment + 1]) {
                    // we are not in the last fragment && the current sample is the start sample of the next fragment
                    currentFragment++;
                }
                durations[currentFragment] += delta;


            }
        }
        return durations;

    }

    public long getBitrate(Track track) {
        long bitrate = 0;
        for (Sample sample : track.getSamples()) {
            bitrate += sample.getSize();
        }
        bitrate /= ((double) track.getDuration()) / track.getTrackMetaData().getTimescale(); // per second
        bitrate *= (long) 8; // from bytes to bits
        return bitrate;
    }


    protected long[] checkFragmentsAlign(long[] referenceTimes, long[] checkTimes) throws IOException {

        if (referenceTimes == null || referenceTimes.length == 0) {
            return checkTimes;
        }
        long[] referenceTimesMinusLast = new long[referenceTimes.length - 1];
        System.arraycopy(referenceTimes, 0, referenceTimesMinusLast, 0, referenceTimes.length - 1);
        long[] checkTimesMinusLast = new long[checkTimes.length - 1];
        System.arraycopy(checkTimes, 0, checkTimesMinusLast, 0, checkTimes.length - 1);

        if (!Arrays.equals(checkTimesMinusLast, referenceTimesMinusLast)) {
            String log = "";
            log += (referenceTimes.length);
            log += ("Reference     :  [");
            for (long l : referenceTimes) {
                log += (String.format("%10d,", l));
            }
            log += ("]");
            LOG.warning(log);
            log = "";

            log += (checkTimes.length);
            log += ("Current       :  [");
            for (long l : checkTimes) {
                log += (String.format("%10d,", l));
            }
            log += ("]");
            LOG.warning(log);
            throw new IOException("Track does not have the same fragment borders as its predecessor.");

        } else {
            return checkTimes;
        }
    }

    protected String getFormat(AbstractSampleEntry se) {
        String type = se.getType();
        if (type.equals("encv") || type.equals("enca") || type.equals("encv")) {
            OriginalFormatBox frma = se.getBoxes(OriginalFormatBox.class, true).get(0);
            type = frma.getDataFormat();
        }
        return type;
    }
}
