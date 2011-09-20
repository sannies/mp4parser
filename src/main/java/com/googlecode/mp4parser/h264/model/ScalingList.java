/* Copyright */
package com.googlecode.mp4parser.h264.model;

import com.googlecode.mp4parser.h264.read.CAVLCReader;
import com.googlecode.mp4parser.h264.write.CAVLCWriter;

import java.io.IOException;

/**
 * Scaling list entity
 * <p/>
 * capable to serialize / deserialize with CAVLC bitstream
 *
 * @author Stanislav Vitvitskiy
 */
public class ScalingList {

    public int[] scalingList;
    public boolean useDefaultScalingMatrixFlag;

    public void write(CAVLCWriter out) throws IOException {
        if (useDefaultScalingMatrixFlag) {
            out.writeSE(0, "SPS: ");
            return;
        }

        int lastScale = 8;
        int nextScale = 8;
        for (int j = 0; j < scalingList.length; j++) {
            if (nextScale != 0) {
                int deltaScale = scalingList[j] - lastScale - 256;
                out.writeSE(deltaScale, "SPS: ");
            }
            lastScale = scalingList[j];
        }
    }

    public static ScalingList read(CAVLCReader is, int sizeOfScalingList)
            throws IOException {

        ScalingList sl = new ScalingList();
        sl.scalingList = new int[sizeOfScalingList];
        int lastScale = 8;
        int nextScale = 8;
        for (int j = 0; j < sizeOfScalingList; j++) {
            if (nextScale != 0) {
                int deltaScale = is.readSE("deltaScale");
                nextScale = (lastScale + deltaScale + 256) % 256;
                sl.useDefaultScalingMatrixFlag = (j == 0 && nextScale == 0);
            }
            sl.scalingList[j] = nextScale == 0 ? lastScale : nextScale;
            lastScale = sl.scalingList[j];
        }
        return sl;
    }
}