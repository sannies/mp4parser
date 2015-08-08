package com.mp4parser.tools;


import com.mp4parser.Box;
import com.mp4parser.Container;
import com.mp4parser.ParsableBox;

public class Offsets {
    public static long find(Container container, ParsableBox target, long offset) {
        long nuOffset = offset;
        for (Box lightBox : container.getBoxes()) {
            if (lightBox == target) {
                return nuOffset;
            }
            if (lightBox instanceof Container) {
                long r = find((Container) lightBox, target, 0);
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
