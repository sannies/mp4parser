package com.mp4parser.streaming.rawformats.h264;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

/**
 * Reads H264 data from an Annex B InputStream.
 */
public class H264AnnexBTrack extends H264NalConsumingTrack implements Callable<Void> {
    private boolean closed = false;
    private InputStream inputStream;

    CountDownLatch countDownLatch = new CountDownLatch(1);

    public H264AnnexBTrack(InputStream inputStream) throws IOException {
        assert inputStream != null;
        this.inputStream = inputStream;
    }

    @Override
    public boolean sourceDepleted() {
        try {
            return inputStream.available() == 0;
        } catch (IOException e) {
            return true;
        }
    }

    public void close() throws IOException {
        closed = true;
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    public Void call() throws IOException {
        byte[] nal;
        NalStreamTokenizer st = new NalStreamTokenizer(inputStream, new byte[]{0, 0, 1}, new byte[]{0, 0, 0});

        while ((nal = st.getNext()) != null && !closed) {
            //System.err.println("NAL before consume");
            consumeNal(nal);
            //System.err.println("NAL after consume");
        }
        drainDecPictureBuffer(true);
        countDownLatch.countDown();
        return null;
    }

    public static class NalStreamTokenizer {
        private static final Logger LOG = Logger.getLogger(NalStreamTokenizer.class.getName());
        private InputStream inputStream;

        private byte[] startPattern;
        private byte[] stopPattern;
        private byte[] buffer;


        private enum State {
            IN, OUT, DONE
        }

        private State state;

        public NalStreamTokenizer(InputStream inputStream, byte[] startPattern, byte[] stopPattern) {
            this.inputStream = inputStream;
            this.startPattern = startPattern;
            this.stopPattern = stopPattern;
            buffer = new byte[startPattern.length];
            Arrays.fill(buffer, (byte) 0x3f);
            state = State.OUT;
        }

        public byte[] getNext() throws IOException {
            LOG.finest("getNext() called");
            ByteArrayOutputStream next = new ByteArrayOutputStream();
            switch (state) {
                case IN:
                    while (!(Arrays.equals(buffer, startPattern) || Arrays.equals(buffer, stopPattern))) {
                        int c = inputStream.read();
                        if (c == -1) {
                            state = State.DONE;
                            next.write(buffer);
                            return next.toByteArray();
                        } else {
                            next.write(buffer[0]);
                            System.arraycopy(buffer, 1, buffer, 0, buffer.length - 1);
                            buffer[buffer.length - 1] = (byte) c;
                        }
                    }
                    state = State.OUT;
                    return next.toByteArray();
                case OUT:
                    while (!Arrays.equals(buffer, startPattern)) {
                        int c = inputStream.read();
                        if (c == -1) {
                            state = State.DONE;
                            return null;
                        } else {
                            System.arraycopy(buffer, 1, buffer, 0, buffer.length - 1);
                            buffer[buffer.length - 1] = (byte) c;
                        }
                    }
                    // drain stopword from buffer
                    for (int i = 0; i < buffer.length; i++) {
                        int c = inputStream.read();
                        if (c == -1) {
                            state = State.DONE;
                            return null;
                        } else {
                            System.arraycopy(buffer, 1, buffer, 0, buffer.length - 1);
                            buffer[buffer.length - 1] = (byte) c;
                        }
                    }
                    state = State.IN;
                    return getNext();
                case DONE:
                    return null;

            }
            return null;
        }
    }
}
