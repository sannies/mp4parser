package com.coremedia.iso.boxes;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;
import com.googlecode.mp4parser.util.Matrix;

import java.util.Date;
import java.util.Map;


public class MovieHeaderBoxTest extends BoxWriteReadBase<MovieHeaderBox> {
    @Override
    public Class<MovieHeaderBox> getBoxUnderTest() {
        return MovieHeaderBox.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, MovieHeaderBox box) {
        addPropsHere.put("creationTime", new Date(1369296286000L));
        addPropsHere.put("currentTime", (int) 2342);
        addPropsHere.put("duration", (long) 243423);
        addPropsHere.put("matrix", Matrix.ROTATE_270);
        addPropsHere.put("modificationTime", new Date(1369296286000L));
        addPropsHere.put("nextTrackId", (long) 5543);
        addPropsHere.put("posterTime", 5433);
        addPropsHere.put("previewDuration", 5343);
        addPropsHere.put("previewTime", 666);
        addPropsHere.put("rate", (double) 1);
        addPropsHere.put("selectionDuration", 32);
        addPropsHere.put("selectionTime", 4456);
        addPropsHere.put("timescale", (long) 7565);
        addPropsHere.put("volume", (float) 1.0);
    }
}
