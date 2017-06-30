package org.mp4parser.muxer.tracks;

import org.mp4parser.Box;
import org.mp4parser.IsoFile;
import org.mp4parser.boxes.iso14496.part12.SampleDescriptionBox;
import org.mp4parser.boxes.iso14496.part15.AvcConfigurationBox;
import org.mp4parser.boxes.sampleentry.SampleEntry;
import org.mp4parser.boxes.sampleentry.VisualSampleEntry;
import org.mp4parser.muxer.Sample;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.WrappingTrack;
import org.mp4parser.tools.ByteBufferByteChannel;
import org.mp4parser.tools.IsoTypeWriterVariable;
import org.mp4parser.tools.Path;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.*;

import static org.mp4parser.tools.CastUtils.l2i;

/**
 * Converts an avc1 track to an avc3 track. The major difference is the location of SPS/PPS: While the avc1 track
 * has all SPS/PPS in the <code>SampleEntry</code> the avc3 track has all required SPS/PPS include in each sync sample.
 */
public class Avc1ToAvc3TrackImpl extends WrappingTrack {

    List<Sample> samples;
    Map<SampleEntry, SampleEntry> avc1toavc3 = new LinkedHashMap<>();

    public Avc1ToAvc3TrackImpl(Track parent) throws IOException {
        super(parent);
        if (!"avc1".equals(parent.getSampleDescriptionBox().getSampleEntry().getType())) {
            throw new RuntimeException("Only avc1 tracks can be converted to avc3 tracks");
        }
        boolean foundAvc1 = false;

        for (SampleEntry sampleEntry : parent.getSampleDescriptionBox().getBoxes(SampleEntry.class)) {
            if (sampleEntry.getType().equals("avc1")) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    // This creates a copy cause I can't change the original instance
                    sampleEntry.getBox(Channels.newChannel(baos));
                    VisualSampleEntry avc3SampleEntry = (VisualSampleEntry) new IsoFile(new ByteBufferByteChannel(ByteBuffer.wrap(baos.toByteArray()))).getBoxes().get(0);
                    avc3SampleEntry.setType("avc3");
                    avc1toavc3.put(sampleEntry, avc3SampleEntry);
                    foundAvc1 = true;
                } catch (IOException e) {
                    throw new RuntimeException("Dumping sample entry to memory failed");
                }
            } else {
                avc1toavc3.put(sampleEntry, sampleEntry);
            }

        }

        assert foundAvc1: "There was no avc1 sample entry to convert it to avc3";


        samples = new ReplaceSyncSamplesList(parent.getSamples());
    }

    public SampleDescriptionBox getSampleDescriptionBox() {
        SampleDescriptionBox stsd = new SampleDescriptionBox();
        stsd.setBoxes(new ArrayList<>(new ArrayList<Box>(avc1toavc3.values())));
        return stsd;
    }

    public List<Sample> getSamples() {
        return samples;
    }

    private class ReplaceSyncSamplesList extends AbstractList<Sample> {
        List<Sample> parentSamples;

        public ReplaceSyncSamplesList(List<Sample> parentSamples) {
            this.parentSamples = parentSamples;
        }

        @Override
        public Sample get(final int index) {
            if (Arrays.binarySearch(Avc1ToAvc3TrackImpl.this.getSyncSamples(), index + 1) >= 0) {
                final Sample orignalSample = parentSamples.get(index);
                final AvcConfigurationBox avcC = orignalSample.getSampleEntry().getBoxes(AvcConfigurationBox.class).get(0);
                final int len = avcC.getLengthSizeMinusOne() + 1;
                final ByteBuffer buf = ByteBuffer.allocate(len);

                final SampleEntry se = avc1toavc3.get(orignalSample.getSampleEntry());


                return new Sample() {

                    public SampleEntry getSampleEntry() {
                        return se;
                    }

                    public void writeTo(WritableByteChannel channel) throws IOException {

                        for (ByteBuffer bytes : avcC.getSequenceParameterSets()) {
                            IsoTypeWriterVariable.write(bytes.limit(), (ByteBuffer) buf.rewind(), len);
                            channel.write((ByteBuffer) buf.rewind());
                            channel.write(bytes);
                        }
                        for (ByteBuffer bytes : avcC.getSequenceParameterSetExts()) {
                            IsoTypeWriterVariable.write(bytes.limit(), (ByteBuffer) buf.rewind(), len);
                            channel.write((ByteBuffer) buf.rewind());
                            channel.write((bytes));
                        }
                        for (ByteBuffer bytes : avcC.getPictureParameterSets()) {
                            IsoTypeWriterVariable.write(bytes.limit(), (ByteBuffer) buf.rewind(), len);
                            channel.write((ByteBuffer) buf.rewind());
                            channel.write((bytes));
                        }
                        orignalSample.writeTo(channel);
                    }

                    public long getSize() {

                        int spsPpsSize = 0;
                        for (ByteBuffer bytes : avcC.getSequenceParameterSets()) {
                            spsPpsSize += len + bytes.limit();
                        }
                        for (ByteBuffer bytes : avcC.getSequenceParameterSetExts()) {
                            spsPpsSize += len + bytes.limit();
                        }
                        for (ByteBuffer bytes : avcC.getPictureParameterSets()) {
                            spsPpsSize += len + bytes.limit();
                        }
                        return orignalSample.getSize() + spsPpsSize;
                    }

                    public ByteBuffer asByteBuffer() {

                        int spsPpsSize = 0;
                        for (ByteBuffer bytes : avcC.getSequenceParameterSets()) {
                            spsPpsSize += len + bytes.limit();
                        }
                        for (ByteBuffer bytes : avcC.getSequenceParameterSetExts()) {
                            spsPpsSize += len + bytes.limit();
                        }
                        for (ByteBuffer bytes : avcC.getPictureParameterSets()) {
                            spsPpsSize += len + bytes.limit();
                        }


                        ByteBuffer data = ByteBuffer.allocate(l2i(orignalSample.getSize()) + spsPpsSize);
                        for (ByteBuffer bytes : avcC.getSequenceParameterSets()) {
                            IsoTypeWriterVariable.write(bytes.limit(), data, len);
                            data.put(bytes);
                        }
                        for (ByteBuffer bytes : avcC.getSequenceParameterSetExts()) {
                            IsoTypeWriterVariable.write(bytes.limit(), data, len);
                            data.put(bytes);
                        }
                        for (ByteBuffer bytes : avcC.getPictureParameterSets()) {
                            IsoTypeWriterVariable.write(bytes.limit(), data, len);
                            data.put(bytes);
                        }
                        data.put(orignalSample.asByteBuffer());
                        return (ByteBuffer) data.rewind();
                    }
                };

            } else {
                return parentSamples.get(index);
            }
        }

        @Override
        public int size() {
            return parentSamples.size();
        }
    }

}
