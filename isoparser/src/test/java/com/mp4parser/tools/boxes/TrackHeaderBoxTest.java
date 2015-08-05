package com.mp4parser.tools.boxes;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;
import com.mp4parser.support.Matrix;
import com.mp4parser.boxes.iso14496.part12.TrackHeaderBox;

import java.util.Date;
import java.util.Map;

public class TrackHeaderBoxTest extends BoxWriteReadBase<TrackHeaderBox> {
    @Override
    public Class<TrackHeaderBox> getBoxUnderTest() {
        return TrackHeaderBox.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, TrackHeaderBox box) {
        addPropsHere.put("alternateGroup", (int) 2);
        addPropsHere.put("creationTime", new Date(1369296286000L));
        addPropsHere.put("duration", (long) 423);
        addPropsHere.put("enabled", true);
        addPropsHere.put("height", 480.0);
        addPropsHere.put("inMovie", true);
        addPropsHere.put("inPoster", false);
        addPropsHere.put("inPreview", true);
        addPropsHere.put("layer", (int) 213);
        addPropsHere.put("matrix", Matrix.ROTATE_180);
        addPropsHere.put("modificationTime", new Date(1369296386000L));
        addPropsHere.put("trackId", (long) 23423);
        addPropsHere.put("volume", (float) 1.0);
        addPropsHere.put("width", 640.0);
    }
}
