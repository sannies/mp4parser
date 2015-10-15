package org.mp4parser.streaming;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class AsyncWritableByteChannel implements WritableByteChannel {


    final BlockingQueue<ByteBuffer> writeBuffer = new LinkedBlockingQueue<ByteBuffer>();
    private final WritableByteChannel target;
    IOException ioException;
    Thread writer = new Thread() {
        @Override
        public void run() {
            try {
                while (target.isOpen()) {
                    synchronized (writeBuffer) {
                        ByteBuffer b = writeBuffer.poll(1, TimeUnit.MILLISECONDS);
                        if (b != null) {

                            target.write(b);
                            if (writeBuffer.isEmpty()) {
                                writeBuffer.notify();
                            }

                        }
                    }
                }
            } catch (InterruptedException e) {
                // this is how we terminated this
            } catch (IOException e) {
                ioException = e;
            }

        }
    };

    public AsyncWritableByteChannel(WritableByteChannel target) {
        this.target = target;
        writer.start();
    }

    public int write(ByteBuffer src) throws IOException {
        if (ioException != null) {
            throw ioException;
        }
        writeBuffer.add(src);
        return src.remaining();
    }

    public boolean isOpen() {
        return target.isOpen();
    }

    public void close() throws IOException {
        synchronized (writeBuffer) {
            while (!writeBuffer.isEmpty() && target.isOpen()) {
                try {
                    writeBuffer.wait();
                } catch (InterruptedException e) {

                }
            }
        }
        writer.interrupt();
    }
}
