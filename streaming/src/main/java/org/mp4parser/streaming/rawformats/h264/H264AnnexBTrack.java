package org.mp4parser.streaming.rawformats.h264;

import org.mp4parser.streaming.extensions.TrackIdTrackExtension;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reads H264 data from an Annex B InputStream.
 */
public class H264AnnexBTrack extends H264NalConsumingTrack implements Callable<Void> {
    CountDownLatch countDownLatch = new CountDownLatch(1);
    private boolean closed = false;
    private InputStream inputStream;

    public H264AnnexBTrack(InputStream inputStream) throws IOException {
        assert inputStream != null;
        this.inputStream = new BufferedInputStream(inputStream);
    }

    public boolean isClosed() {
        return closed;
    }

    public void close() throws IOException {
        closed = true;
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    public Void call() throws IOException, InterruptedException {
        byte[] nal;
        NalStreamTokenizer st = new NalStreamTokenizer(inputStream);

        while ((nal = st.getNext()) != null && !closed) {
            //System.err.println("NAL before consume");
            consumeNal(nal);
            //System.err.println("NAL after consume");
        }
        drainDecPictureBuffer(true);
        closed = true;
        countDownLatch.countDown();
        return null;
    }

    @Override
    public String toString() {
        TrackIdTrackExtension trackIdTrackExtension = this.getTrackExtension(TrackIdTrackExtension.class);
        if (trackIdTrackExtension != null) {
            return "H264AnnexBTrack{trackId=" + trackIdTrackExtension.getTrackId() + "}";
        } else {
            return "H264AnnexBTrack{}";
        }
    }

    public static class NalStreamTokenizer {
        private static final Logger LOG = Logger.getLogger(NalStreamTokenizer.class.getName());
        MyByteArrayOutputStream next = new MyByteArrayOutputStream();
        private InputStream inputStream;
        private State state;

        public NalStreamTokenizer(InputStream inputStream) {
            this.inputStream = inputStream;
            state = State.OUT;
        }

        public byte[] getNext() throws IOException {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.finest("getNext() called");
            }

            outerwhile:
            while (true) {

                switch (state) {
                    case IN:
                        int c;
                        while ((c = inputStream.read()) != -1) {
                            next.write(c);
                            if (next.lastThreeEqual((byte) 0, (byte) 0, (byte) 1)) {
                                // this closes last NAL and open new NAL at the same time
                                state = State.IN;
                                byte[] s = next.toByteArrayLess3();
                                next.reset();
                                return s;
                            }
                            if (next.lastThreeEqual((byte) 0, (byte) 0, (byte) 0)) {
                                state = State.OUT;
                                byte[] s = next.toByteArrayLess3();
                                next.keepLast3();
                                return s;
                            }

                        }
                        state = State.DONE;
                        return next.toByteArray();
                    case OUT:

                        while ((c = inputStream.read()) != -1) {
                            next.write(c);
                            if (next.lastThreeEqual((byte) 0, (byte) 0, (byte) 1)) {
                                state = State.IN;
                                next.reset();
                                continue outerwhile;
                            }
                        }
                        state = State.DONE;
                    case DONE:
                        return null;

                }
            }
        }

        private enum State {
            IN, OUT, DONE
        }
    }

    static class MyByteArrayOutputStream extends ByteArrayOutputStream {
        public boolean lastThreeEqual(byte a, byte b, byte c) {
            int i = count;
            return i > 3 && buf[count - 3] == a && buf[count - 2] == b && buf[count - 1] == c;
        }

        public byte[] toByteArrayLess3() {
            return Arrays.copyOf(buf, count - 3 > 0 ? count - 3 : 0);

        }

        public void keepLast3() {
            buf[0] = buf[count - 3];
            buf[1] = buf[count - 2];
            buf[2] = buf[count - 1];
            count = 3;
        }
    }
}
