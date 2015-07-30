package com.googlecode.mp4parser.util;

import java.lang.reflect.Array;

/**
 * Created by sannies on 02.05.2015.
 */
public final class Mp4Arrays {
    private Mp4Arrays() {
    }

    public static long[] copyOfAndAppend(long[] original, long... toAppend) {
        if (original == null) {
            original = new long[]{};
        }
        if (toAppend == null) {
            toAppend = new long[]{};
        }
        long[] copy = new long[original.length + toAppend.length];
        System.arraycopy(original, 0, copy, 0, original.length);
        System.arraycopy(toAppend, 0, copy, original.length, toAppend.length);
        return copy;
    }


    public static int[] copyOfAndAppend(int[] original, int... toAppend) {
        if (original == null) {
            original = new int[]{};
        }
        if (toAppend == null) {
            toAppend = new int[]{};
        }
        int[] copy = new int[original.length + toAppend.length];
        System.arraycopy(original, 0, copy, 0, original.length);
        System.arraycopy(toAppend, 0, copy, original.length, toAppend.length);
        return copy;
    }

}
