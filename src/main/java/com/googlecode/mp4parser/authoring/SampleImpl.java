package com.googlecode.mp4parser.authoring;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

public class SampleImpl implements Sample {
	
	private final ByteBuffer[] data;
	
	public SampleImpl(ByteBuffer buf) {
		this.data = new ByteBuffer[] { buf };
	}

	public SampleImpl(ByteBuffer[] data) {
		this.data = data;
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

}
