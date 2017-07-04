package org.mp4parser.muxer.tracks.mjpeg;

import org.mp4parser.boxes.iso14496.part1.objectdescriptors.ESDescriptor;
import org.mp4parser.boxes.iso14496.part1.objectdescriptors.ObjectDescriptorFactory;
import org.mp4parser.boxes.iso14496.part12.CompositionTimeToSample;
import org.mp4parser.boxes.iso14496.part12.SampleDescriptionBox;
import org.mp4parser.boxes.iso14496.part14.ESDescriptorBox;
import org.mp4parser.boxes.sampleentry.SampleEntry;
import org.mp4parser.boxes.sampleentry.VisualSampleEntry;
import org.mp4parser.muxer.*;
import org.mp4parser.tools.Hex;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by sannies on 13.02.2015.
 */
public class OneJpegPerIframe extends AbstractTrack {
    private File[] jpegs;
    private TrackMetaData trackMetaData = new TrackMetaData();
    private long[] sampleDurations;
    private long[] syncSamples;
    private VisualSampleEntry mp4v;


    public OneJpegPerIframe(String name, File[] jpegs, Track alignTo) throws IOException {
        super(name);
        this.jpegs = jpegs;
        if (alignTo.getSyncSamples().length != jpegs.length) {
            throw new RuntimeException("Number of sync samples doesn't match the number of stills (" + alignTo.getSyncSamples().length + " vs. " + jpegs.length + ")");
        }
        BufferedImage a = ImageIO.read(jpegs[0]);
        trackMetaData.setWidth(a.getWidth());
        trackMetaData.setHeight(a.getHeight());
        trackMetaData.setTimescale(alignTo.getTrackMetaData().getTimescale());


        long[] sampleDurationsToiAlignTo = alignTo.getSampleDurations();
        long[] syncSamples = alignTo.getSyncSamples();
        int currentSyncSample = 1;
        long duration = 0;
        sampleDurations = new long[syncSamples.length];

        for (int i = 1; i < sampleDurationsToiAlignTo.length; i++) {
            if (currentSyncSample < syncSamples.length && i == syncSamples[currentSyncSample]) {
                sampleDurations[currentSyncSample - 1] = duration;
                duration = 0;
                currentSyncSample++;
            }
            duration += sampleDurationsToiAlignTo[i];
        }
        sampleDurations[sampleDurations.length - 1] = duration;

        mp4v = new VisualSampleEntry("mp4v");
        ESDescriptorBox esds = new ESDescriptorBox();
        esds.setData(ByteBuffer.wrap(Hex.decodeHex("038080801B000100048080800D6C11000000000A1CB4000A1CB4068080800102")));
        esds.setEsDescriptor((ESDescriptor) ObjectDescriptorFactory.createFrom(-1, ByteBuffer.wrap(Hex.decodeHex("038080801B000100048080800D6C11000000000A1CB4000A1CB4068080800102"))));
        mp4v.addBox(esds);
        this.syncSamples = new long[jpegs.length];
        for (int i = 0; i < this.syncSamples.length; i++) {
            this.syncSamples[i] = i + 1;

        }

        double earliestTrackPresentationTime = 0;
        boolean acceptDwell = true;
        boolean acceptEdit = true;
        for (Edit edit : alignTo.getEdits()) {
            if (edit.getMediaTime() == -1 && !acceptDwell) {
                throw new RuntimeException("Cannot accept edit list for processing (1)");
            }
            if (edit.getMediaTime() >= 0 && !acceptEdit) {
                throw new RuntimeException("Cannot accept edit list for processing (2)");
            }
            if (edit.getMediaTime() == -1) {
                earliestTrackPresentationTime += edit.getSegmentDuration();
            } else /* if edit.getMediaTime() >= 0 */ {
                earliestTrackPresentationTime -= (double) edit.getMediaTime() / edit.getTimeScale();
                acceptEdit = false;
                acceptDwell = false;
            }
        }
        if (alignTo.getCompositionTimeEntries() != null && alignTo.getCompositionTimeEntries().size() > 0) {
            long currentTime = 0;
            int[] ptss = CompositionTimeToSample.blowupCompositionTimes(alignTo.getCompositionTimeEntries());
            for (int j = 0; j < ptss.length && j < 50; j++) {
                ptss[j] += currentTime;
                currentTime += alignTo.getSampleDurations()[j];
            }
            Arrays.sort(ptss);
            earliestTrackPresentationTime += (double) ptss[0] / alignTo.getTrackMetaData().getTimescale();

        }

        if (earliestTrackPresentationTime < 0) {
            getEdits().add(new Edit((long) (-earliestTrackPresentationTime * getTrackMetaData().getTimescale()), getTrackMetaData().getTimescale(), 1.0, (double) getDuration() / getTrackMetaData().getTimescale()));
        } else if (earliestTrackPresentationTime > 0) {
            getEdits().add(new Edit(-1, getTrackMetaData().getTimescale(), 1.0, earliestTrackPresentationTime));
            getEdits().add(new Edit(0, getTrackMetaData().getTimescale(), 1.0, (double) getDuration() / getTrackMetaData().getTimescale()));
        }

    }

    public List<SampleEntry> getSampleEntries() {
        return Collections.<SampleEntry>singletonList(mp4v);
    }

    public long[] getSampleDurations() {
        return sampleDurations;
    }

    public TrackMetaData getTrackMetaData() {
        return trackMetaData;
    }

    public String getHandler() {
        return "vide";
    }

    @Override
    public long[] getSyncSamples() {
        return syncSamples;
    }


    public List<Sample> getSamples() {
        return new AbstractList<Sample>() {

            @Override
            public int size() {
                return jpegs.length;
            }

            @Override
            public Sample get(final int index) {
                return new Sample() {
                    ByteBuffer sample = null;

                    public void writeTo(WritableByteChannel channel) throws IOException {
                        RandomAccessFile raf = new RandomAccessFile(jpegs[index], "r");
                        raf.getChannel().transferTo(0, raf.length(), channel);
                        raf.close();
                    }

                    public long getSize() {
                        return jpegs[index].length();
                    }

                    public ByteBuffer asByteBuffer() {
                        if (sample == null) {
                            try {
                                RandomAccessFile raf = new RandomAccessFile(jpegs[index], "r");
                                sample = raf.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, raf.length());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        return sample;
                    }

                    @Override
                    public SampleEntry getSampleEntry() {
                        return mp4v;
                    }
                };
            }
        };
    }

    public void close() throws IOException {

    }
}
