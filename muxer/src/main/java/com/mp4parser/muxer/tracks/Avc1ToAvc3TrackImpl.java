package com.mp4parser.muxer.tracks;

import com.mp4parser.IsoFile;
import com.mp4parser.muxer.Sample;
import com.mp4parser.muxer.Track;
import com.mp4parser.muxer.WrappingTrack;
import com.mp4parser.tools.IsoTypeWriterVariable;
import com.mp4parser.boxes.iso14496.part12.SampleDescriptionBox;
import com.mp4parser.boxes.sampleentry.VisualSampleEntry;
import com.mp4parser.tools.ByteBufferByteChannel;
import com.mp4parser.tools.Path;
import com.mp4parser.boxes.iso14496.part15.AvcConfigurationBox;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;

import static com.mp4parser.tools.CastUtils.l2i;

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
        IsoFile isoFile = new IsoFile(new ByteBufferByteChannel(ByteBuffer.wrap(baos.toByteArray())));
        this.stsd = Path.getPath(isoFile, "stsd");
        assert stsd != null;
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
