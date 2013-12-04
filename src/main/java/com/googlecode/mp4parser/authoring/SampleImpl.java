package com.googlecode.mp4parser.authoring;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;

public class SampleImpl implements Sample {
	private final long offset;
	private final ByteBuffer[] data;
	
	public SampleImpl(ByteBuffer buf) {
        this.offset = -1;
		this.data = new ByteBuffer[] { buf };
	}

	public SampleImpl(ByteBuffer[] data) {
        this.offset = -1;
        this.data = data;
	}

    public SampleImpl(long offset, ByteBuffer data) {
        this.offset = offset;
        this.data = new ByteBuffer[] { data };
    }

    public void writeTo(WritableByteChannel channel) throws IOException {
		for(ByteBuffer b : data) {
    		channel.write(b);
		}
	}
	
	public long remaining() {
		long r = 0;
		for(ByteBuffer b : data) {
			r += b.remaining();
		}
		return r;
	}
	
	public ByteBuffer asByteBuffer() {
		byte[] bCopy = new byte[(int) remaining()];
		ByteBuffer copy = ByteBuffer.wrap(bCopy);
		for(ByteBuffer b : data) {
			copy.put(b.duplicate());
		}
		return copy;
	}

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("SampleImpl");
        sb.append("{offset=").append(offset);
        sb.append('}');
        return sb.toString();
    }
}
