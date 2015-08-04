package com.mp4parser.tools;


import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.Container;
import com.mp4parser.LightBox;

public class Offsets {
    public static long find(Container container, Box target, long offset) {
        long nuOffset = offset;
        for (LightBox lightBox : container.getBoxes()) {
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
