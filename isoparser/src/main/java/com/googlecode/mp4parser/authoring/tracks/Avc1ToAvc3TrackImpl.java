package com.googlecode.mp4parser.authoring.tracks;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoTypeWriterVariable;
import com.coremedia.iso.boxes.CompositionTimeToSample;
import com.coremedia.iso.boxes.SampleDependencyTypeBox;
import com.coremedia.iso.boxes.SampleDescriptionBox;
import com.coremedia.iso.boxes.SubSampleInformationBox;
import com.coremedia.iso.boxes.sampleentry.VisualSampleEntry;
import com.googlecode.mp4parser.MemoryDataSourceImpl;
import com.googlecode.mp4parser.authoring.*;
import com.googlecode.mp4parser.boxes.mp4.samplegrouping.GroupEntry;
import com.googlecode.mp4parser.util.Path;
import com.mp4parser.iso14496.part15.AvcConfigurationBox;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.googlecode.mp4parser.util.CastUtils.l2i;

/**
 * Converts an avc1 track to an avc3 track. The major difference is the location of SPS/PPS: While the avc1 track
 * has all SPS/PPS in the <code>SampleEntry</code> the avc3 track has all required SPS/PPS include in each sync sample.
 */
public class Avc1ToAvc3TrackImpl extends WrappingTrack {
    SampleDescriptionBox stsd;
    AvcConfigurationBox avcC;
    List<Sample> samples;

    public Avc1ToAvc3TrackImpl(Track parent) throws IOException {
        super(parent);
        if (!"avc1".equals(parent.getSampleDescriptionBox().getSampleEntry().getType())) {
            throw new RuntimeException("Only avc1 tracks can be converted to avc3 tracks");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        parent.getSampleDescriptionBox().getBox(Channels.newChannel(baos));
        IsoFile isoFile = new IsoFile(new MemoryDataSourceImpl(baos.toByteArray()));
        this.stsd = Path.getPath(isoFile, "stsd");
        ((VisualSampleEntry) stsd.getSampleEntry()).setType("avc3");

        avcC = Path.getPath(stsd, "avc./avcC");

        samples = new ReplaceSyncSamplesList(parent.getSamples());
    }

    public SampleDescriptionBox getSampleDescriptionBox() {
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
                final int len = avcC.getLengthSizeMinusOne()+1;
                final ByteBuffer buf = ByteBuffer.allocate(len);
                final Sample orignalSample = parentSamples.get(index);
                return new Sample() {

                    public void writeTo(WritableByteChannel channel) throws IOException {

                        for (byte[] bytes : avcC.getSequenceParameterSets()) {
                            IsoTypeWriterVariable.write(bytes.length, (ByteBuffer) buf.rewind(), len);
                            channel.write((ByteBuffer) buf.rewind());
                            channel.write(ByteBuffer.wrap(bytes));
                        }
                        for (byte[] bytes : avcC.getSequenceParameterSetExts()) {
                            IsoTypeWriterVariable.write(bytes.length, (ByteBuffer) buf.rewind(), len);
                            channel.write((ByteBuffer) buf.rewind());
                            channel.write(ByteBuffer.wrap(bytes));
                        }
                        for (byte[] bytes : avcC.getPictureParameterSets()) {
                            IsoTypeWriterVariable.write(bytes.length, (ByteBuffer) buf.rewind(), len);
                            channel.write((ByteBuffer) buf.rewind());
                            channel.write(ByteBuffer.wrap(bytes));
                        }
                        orignalSample.writeTo(channel);
                    }

                    public long getSize() {

                        int spsPpsSize = 0;
                        for (byte[] bytes : avcC.getSequenceParameterSets()) {
                            spsPpsSize += len + bytes.length;
                        }
                        for (byte[] bytes : avcC.getSequenceParameterSetExts()) {
                            spsPpsSize += len + bytes.length;
                        }
                        for (byte[] bytes : avcC.getPictureParameterSets()) {
                            spsPpsSize += len + bytes.length;
                        }
                        return orignalSample.getSize() + spsPpsSize;
                    }

                    public ByteBuffer asByteBuffer() {

                        int spsPpsSize = 0;
                        for (byte[] bytes : avcC.getSequenceParameterSets()) {
                            spsPpsSize += len + bytes.length;
                        }
                        for (byte[] bytes : avcC.getSequenceParameterSetExts()) {
                            spsPpsSize += len + bytes.length;
                        }
                        for (byte[] bytes : avcC.getPictureParameterSets()) {
                            spsPpsSize += len + bytes.length;
                        }



                        ByteBuffer data = ByteBuffer.allocate (l2i(orignalSample.getSize()) + spsPpsSize);
                        for (byte[] bytes : avcC.getSequenceParameterSets()) {
                            IsoTypeWriterVariable.write(bytes.length, data, len);
                            data.put(bytes);
                        }
                        for (byte[] bytes : avcC.getSequenceParameterSetExts()) {
                            IsoTypeWriterVariable.write(bytes.length, data, len);
                            data.put(bytes);
                        }
                        for (byte[] bytes : avcC.getPictureParameterSets()) {
                            IsoTypeWriterVariable.write(bytes.length, data, len);
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
