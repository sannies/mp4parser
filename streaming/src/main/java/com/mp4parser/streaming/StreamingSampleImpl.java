package com.mp4parser.streaming;

import com.mp4parser.muxer.Sample;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

/**
 * Created by sannies on 17.08.2015.
 */
public class StreamingSampleImpl implements StreamingSample {

    private ByteBuffer s;
    private long duration;
    private HashMap<Class<? extends SampleExtension>, SampleExtension> sampleExtensions = new HashMap<Class<? extends SampleExtension>, SampleExtension>();

    public StreamingSampleImpl(ByteBuffer s, long duration) {
        this.s = s.duplicate();
        this.duration = duration;
    }

    public StreamingSampleImpl(List<byte[]> nals, long duration) {
        this.duration = duration;
        int size = 0;
        for (byte[] nal : nals) {
            size += 4;
            size += nal.length;
        }
        byte sample[] = new byte[size];
        int pos = 0;
        for (byte[] nal : nals) {
            sample[pos] = (byte) ((nal.length & 0xff000000) >> 24);
            sample[pos + 1] = (byte) ((nal.length & 0xff0000) >> 16);
            sample[pos + 2] = (byte) ((nal.length & 0xff00) >> 8);
            sample[pos + 3] = (byte) ((nal.length & 0xff));
            pos += 4;
            System.arraycopy(nal, 0, sample, pos, nal.length);
            pos += nal.length;
        }
        s = ByteBuffer.wrap(sample);


    }

    public ByteBuffer getContent() {
        return s.duplicate();
    }

    public long getDuration() {
        return duration;
    }

    public <T extends SampleExtension> T getSampleExtension(Class<T> clazz) {
        return (T) sampleExtensions.get(clazz);
    }

    public void addSampleExtension(SampleExtension sampleExtension) {
        sampleExtensions.put(sampleExtension.getClass(), sampleExtension);
    }

    public <T extends SampleExtension> T removeSampleExtension(Class<T> clazz) {
        return (T) sampleExtensions.remove(clazz);
    }
}
