package org.mp4parser.muxer.tracks;

import org.mp4parser.boxes.iso14496.part12.CompositionTimeToSample;
import org.mp4parser.boxes.iso14496.part12.SampleDependencyTypeBox;
import org.mp4parser.muxer.*;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Bundles common functionality and parsing patterns of NAL based formats such as H264(AVC) and H265 (HEVC).
 */
public abstract class AbstractH26XTrack extends AbstractTrack {

    public static int BUFFER = 65535 << 10;
    protected long[] decodingTimes;
    protected List<CompositionTimeToSample.Entry> ctts = new ArrayList<CompositionTimeToSample.Entry>();
    protected List<SampleDependencyTypeBox.Entry> sdtp = new ArrayList<SampleDependencyTypeBox.Entry>();
    protected List<Integer> stss = new ArrayList<Integer>();
    protected TrackMetaData trackMetaData = new TrackMetaData();
    boolean tripleZeroIsEndOfSequence = true;
    private DataSource dataSource;

    public AbstractH26XTrack(DataSource dataSource, boolean tripleZeroIsEndOfSequence) {

        super(dataSource.toString());
        this.dataSource = dataSource;
        this.tripleZeroIsEndOfSequence = tripleZeroIsEndOfSequence;
    }

    public AbstractH26XTrack(DataSource dataSource) {
        this(dataSource, true);
    }

    protected static InputStream cleanBuffer(InputStream is) {
        return new CleanInputStream(is);
    }

    protected static byte[] toArray(ByteBuffer buf) {
        buf = buf.duplicate();
        byte[] b = new byte[buf.remaining()];
        buf.get(b, 0, b.length);
        return b;
    }

    public TrackMetaData getTrackMetaData() {
        return trackMetaData;
    }

    protected ByteBuffer findNextNal(LookAhead la) throws IOException {
        try {
            while (!la.nextThreeEquals001()) {
                la.discardByte();
            }
            la.discardNext3AndMarkStart();

            while (!la.nextThreeEquals000or001orEof(tripleZeroIsEndOfSequence)) {
                la.discardByte();
            }
            return la.getNal();
        } catch (EOFException e) {
            return null;
        }
    }

    /**
     * Builds an MP4 sample from a list of NALs. Each NAL will be preceded by its
     * 4 byte (unit32) length.
     *
     * @param nals a list of NALs that form the sample
     * @return sample as it appears in the MP4 file
     */
    protected Sample createSampleObject(List<? extends ByteBuffer> nals) {
        byte[] sizeInfo = new byte[nals.size() * 4];
        ByteBuffer sizeBuf = ByteBuffer.wrap(sizeInfo);
        for (ByteBuffer b : nals) {
            sizeBuf.putInt(b.remaining());
        }

        ByteBuffer[] data = new ByteBuffer[nals.size() * 2];

        for (int i = 0; i < nals.size(); i++) {
            data[2 * i] = ByteBuffer.wrap(sizeInfo, i * 4, 4);
            data[2 * i + 1] = nals.get(i);
        }

        return new SampleImpl(data);
    }

    public long[] getSampleDurations() {
        return decodingTimes;
    }

    public List<CompositionTimeToSample.Entry> getCompositionTimeEntries() {
        return ctts;
    }

    public long[] getSyncSamples() {
        long[] returns = new long[stss.size()];
        for (int i = 0; i < stss.size(); i++) {
            returns[i] = stss.get(i);
        }
        return returns;
    }

    public List<SampleDependencyTypeBox.Entry> getSampleDependencies() {
        return sdtp;
    }

    public void close() throws IOException {
        dataSource.close();
    }

    public static class LookAhead {
        long bufferStartPos = 0;
        int inBufferPos = 0;
        DataSource dataSource;
        ByteBuffer buffer;

        long start;

        public LookAhead(DataSource dataSource) throws IOException {
            this.dataSource = dataSource;
            fillBuffer();
        }

        public void fillBuffer() throws IOException {
            buffer = dataSource.map(bufferStartPos, Math.min(dataSource.size() - bufferStartPos, BUFFER));
        }

        public boolean nextThreeEquals001() throws IOException {
            if (buffer.limit() - inBufferPos >= 3) {
                return (buffer.get(inBufferPos) == 0 &&
                        buffer.get(inBufferPos + 1) == 0 &&
                        buffer.get(inBufferPos + 2) == 1);
            }
            if (bufferStartPos + inBufferPos + 3 >= dataSource.size()) {
                throw new EOFException();
            }
            return false;
        }

        public boolean nextThreeEquals000or001orEof(boolean tripleZeroIsEndOfSequence) throws IOException {
            if (buffer.limit() - inBufferPos >= 3) {
                return ((buffer.get(inBufferPos) == 0 &&
                        buffer.get(inBufferPos + 1) == 0 &&
                        ((buffer.get(inBufferPos + 2) == 0 && tripleZeroIsEndOfSequence) || buffer.get(inBufferPos + 2) == 1)));
            } else {
                if (bufferStartPos + inBufferPos + 3 > dataSource.size()) {
                    return bufferStartPos + inBufferPos == dataSource.size();
                } else {
                    bufferStartPos = start;
                    inBufferPos = 0;
                    fillBuffer();
                    return nextThreeEquals000or001orEof(tripleZeroIsEndOfSequence);
                }
            }
        }

        public void discardByte() {
            inBufferPos++;
        }

        public void discardNext3AndMarkStart() {
            inBufferPos += 3;
            start = bufferStartPos + inBufferPos;
        }

        public ByteBuffer getNal() {
            if (start >= bufferStartPos) {

                buffer.position((int) (start - bufferStartPos));
                Buffer sample = buffer.slice();
                sample.limit((int) (inBufferPos - (start - bufferStartPos)));
                return (ByteBuffer) sample;
            } else {
                throw new RuntimeException("damn! NAL exceeds buffer");
                // this can only happen if NAL is bigger than the buffer
                // and that most likely cannot happen with correct inputs
            }

        }
    }
}