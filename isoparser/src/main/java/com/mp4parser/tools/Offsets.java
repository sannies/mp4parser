package com.mp4parser.tools;


import com.mp4parser.ParsableBox;
import com.mp4parser.RandomAccessSource;
import com.mp4parser.Box;

public class Offsets {
    public static long find(RandomAccessSource.Container container, ParsableBox target, long offset) {
        long nuOffset = offset;
        for (Box lightBox : container.getBoxes()) {
            if (lightBox == target) {
                return nuOffset;
            }
            if (lightBox instanceof RandomAccessSource.Container) {
                long r = find((RandomAccessSource.Container) lightBox, target, 0);
                if (r > 0) {
                    return r + nuOffset;
                } else {
                    nuOffset += lightBox.getSize();
                }
            } else {
                nuOffset += lightBox.getSize();
            }
        }
        return -1;
    }
}
