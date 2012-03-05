package com.googlecode.mp4parser.authoring.builder;

import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Used to merge adjacent byte buffers.
 */
public class ByteBufferHelper {
    public static List<ByteBuffer> mergeAdjacentBuffers(List<ByteBuffer> samples) {
        ArrayList<ByteBuffer> nuSamples = new ArrayList<ByteBuffer>(samples.size());
        for (ByteBuffer buffer : samples) {
            int lastIndex = nuSamples.size() - 1;
            if (lastIndex >= 0 && buffer.hasArray() && nuSamples.get(lastIndex).hasArray() && buffer.array() == nuSamples.get(lastIndex).array() &&
                    nuSamples.get(lastIndex).arrayOffset() + nuSamples.get(lastIndex).limit() == buffer.arrayOffset()) {
                ByteBuffer oldBuffer = nuSamples.remove(lastIndex);
                ByteBuffer nu = ByteBuffer.wrap(buffer.array(), oldBuffer.arrayOffset(), oldBuffer.limit() + buffer.limit()).slice();
                // We need to slice here since wrap([], offset, length) just sets position and not the arrayOffset.
                nuSamples.add(nu);
            } else if (lastIndex >= 0 &&
                    buffer instanceof MappedByteBuffer && nuSamples.get(lastIndex) instanceof MappedByteBuffer &&
                    nuSamples.get(lastIndex).limit() == nuSamples.get(lastIndex).capacity() - buffer.capacity()) {
                // This can go wrong - but will it?
                ByteBuffer oldBuffer = nuSamples.get(lastIndex);
                oldBuffer.limit(buffer.limit() + oldBuffer.limit());
            } else {
                nuSamples.add(buffer);
            }
        }
        return nuSamples;
    }
}
