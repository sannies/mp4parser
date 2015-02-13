package com.googlecode.mp4parser.authoring.tracks.mjpeg;

import com.coremedia.iso.Hex;
import com.coremedia.iso.boxes.SampleDescriptionBox;
import com.coremedia.iso.boxes.sampleentry.MpegSampleEntry;
import com.coremedia.iso.boxes.sampleentry.VisualSampleEntry;
import com.googlecode.mp4parser.authoring.AbstractTrack;
import com.googlecode.mp4parser.authoring.Sample;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.TrackMetaData;
import com.googlecode.mp4parser.boxes.mp4.ESDescriptorBox;
import com.googlecode.mp4parser.boxes.mp4.objectdescriptors.DecoderConfigDescriptor;
import com.googlecode.mp4parser.boxes.mp4.objectdescriptors.ESDescriptor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.AbstractList;
import java.util.List;

/**
 * Created by sannies on 13.02.2015.
 */
public class OneJpegPerIframe extends AbstractTrack {
    File[] jpegs;
    Track alignTo;
    TrackMetaData trackMetaData = new TrackMetaData();
    long[] sampleDurations;
    SampleDescriptionBox stsd;

    public OneJpegPerIframe(String name, File[] jpegs, Track alignTo) throws IOException {
        super(name);
        this.jpegs = jpegs;
        if (alignTo.getSyncSamples().length != jpegs.length) {
            throw new RuntimeException("Number of sync samples doesn't match the number of stills (" + alignTo.getSyncSamples().length  +" vs. " + jpegs.length + ")");
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
                sampleDurations[currentSyncSample] = duration;
                duration = 0;
                currentSyncSample++;
            }
            duration += sampleDurationsToiAlignTo[i];
        }
        sampleDurations[sampleDurations.length-1] = duration;

        stsd = new SampleDescriptionBox();
        VisualSampleEntry mp4v = new VisualSampleEntry("mp4v");
        stsd.addBox(mp4v);
        ESDescriptorBox esds = new ESDescriptorBox();
        esds.setData(ByteBuffer.wrap(Hex.decodeHex("038080801B000100048080800D6C11000000000A1CB4000A1CB4068080800102")));
        mp4v.addBox(esds);

    }

    public SampleDescriptionBox getSampleDescriptionBox() {
        return stsd;
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
                        if (sample==null) {
                            try {
                                RandomAccessFile raf = new RandomAccessFile(jpegs[index], "r");
                                sample = raf.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, raf.length());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        return sample;
                    }
                };
            }
        };
    }

    public void close() throws IOException {

    }
}
