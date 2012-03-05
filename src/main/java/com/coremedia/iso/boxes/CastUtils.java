package com.coremedia.iso.boxes;

/**
 * Created by IntelliJ IDEA.
 * User: sannies
 * Date: 2/9/12
 * Time: 8:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class CastUtils {
    /**
     * Casts a long to an int. In many cases I use a long for a UInt32 but this cannot be used to allocate
     * ByteBuffers or arrays since they restricted to <code>Integer.MAX_VALUE</code> this cast-method will throw
     * a RuntimeException if the cast would cause a loss of information.
     *
     * @param l the long value
     * @return the long value as int
     */
    public static int l2i(long l) {
        if (l > Integer.MAX_VALUE || l < Integer.MIN_VALUE) {
            throw new RuntimeException("A cast to int has gone wrong. Please contact the mp4parser discussion group (" + l + ")");
        }
        return (int) l;
    }
}
