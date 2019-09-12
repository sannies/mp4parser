package org.mp4parser.streaming.input;

import org.mp4parser.streaming.SampleExtension;
import org.mp4parser.streaming.StreamingSample;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

public class StreamingSampleImpl implements StreamingSample {

    private ByteBuffer s;
    private long duration;
    private HashMap<Class<? extends SampleExtension>, SampleExtension> sampleExtensions = new HashMap<Class<? extends SampleExtension>, SampleExtension>();

    public StreamingSampleImpl(ByteBuffer s, long duration) {
        this.s = s.duplicate();
        this.duration = duration;
    }

    public StreamingSampleImpl(byte[] sample, long duration) {
        this.duration = duration;
        s = ByteBuffer.wrap(sample);
    }

    public StreamingSampleImpl(List<ByteBuffer> nals, long duration) {
        this.duration = duration;
        int size = 0;
        for (ByteBuffer nal : nals) {
            size += 4;
            size += nal.limit();
        }
        s = ByteBuffer.allocate(size);

        for (ByteBuffer nal : nals) {
            s.put((byte) ((nal.limit() & 0xff000000) >> 24));
            s.put((byte) ((nal.limit() & 0xff0000) >> 16));
            s.put((byte) ((nal.limit() & 0xff00) >> 8));
            s.put((byte) ((nal.limit() & 0xff)));
            s.put((ByteBuffer) ((Buffer)nal).rewind());
        }


    }

    public ByteBuffer getContent() {
        return s;
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
