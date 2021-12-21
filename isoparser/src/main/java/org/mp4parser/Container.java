package org.mp4parser;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.util.List;

/**
 * Interface for all ISO boxes that may contain other boxes.
 */
public interface Container {

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
     * @param <T>   type of boxes to get
     * @return an array of boxes, empty array in case of no children.
     */
    <T extends Box> List<T> getBoxes(Class<T> clazz);

    /**
     * Gets all child boxes of the given type. May not return <code>null</code>.
     *
     * @param clazz     child box's type
     * @param recursive step down the tree
     * @param <T>       type of boxes to get
     * @return an array of boxes, empty array in case of no children.
     */
    <T extends Box> List<T> getBoxes(Class<T> clazz, boolean recursive);

    void writeContainer(WritableByteChannel bb) throws IOException;
}