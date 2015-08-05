package com.mp4parser.tools;


/**
 * A little helper for working with arrays as some functions now available in Java 7/8 are
 * not available on all Android platforms.
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

    public static byte[] copyOfAndAppend(byte[] original, byte... toAppend) {
        if (original == null) {
            original = new byte[]{};
        }
        if (toAppend == null) {
            toAppend = new byte[]{};
        }
        byte[] copy = new byte[original.length + toAppend.length];
        System.arraycopy(original, 0, copy, 0, original.length);
        System.arraycopy(toAppend, 0, copy, original.length, toAppend.length);
        return copy;
    }

}
