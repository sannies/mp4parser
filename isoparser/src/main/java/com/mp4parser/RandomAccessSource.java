package com.mp4parser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.List;

/**
 * Allows random access to some data source as some data structure such as chunks in an mdat
 * require random access with absolute offsets.
 */
public interface RandomAccessSource {
    ByteBuffer get(long offset, long size) throws IOException;

    /**
     * Interface for all ISO boxes that may contain other boxes.
     */
    interface Container {

        /**
         * Gets all child boxes. May not return <code>null</code>.
         *
         * @return an array of boxes, empty array in case of no children.
         */
        List<Box> getBoxes();

        /**
         * Sets all boxes and removes all previous child boxes.
         *
         * @param boxes the new list of children
         */
        void setBoxes(List<? extends Box> boxes);

        /**
         * Gets all child boxes of the given type. May not return <code>null</code>.
         *
         * @param clazz child box's type
         * @param <T> type of boxes to get
         * @return an array of boxes, empty array in case of no children.
         */
        <T extends Box> List<T> getBoxes(Class<T> clazz);

        /**
         * Gets all child boxes of the given type. May not return <code>null</code>.
         *
         * @param clazz     child box's type
         * @param recursive step down the tree
         * @param <T> type of boxes to get
         * @return an array of boxes, empty array in case of no children.
         */
        <T extends Box> List<T> getBoxes(Class<T> clazz, boolean recursive);

        void writeContainer(WritableByteChannel bb) throws IOException;
    }

    /**
     * The <code>FullBox</code> contains all getters and setters specific
     * to a so-called full box according to the ISO/IEC 14496/12 specification.
     */
    interface FullBox extends ParsableBox {
        int getVersion();

        void setVersion(int version);

        int getFlags();

        void setFlags(int flags);
    }
}
