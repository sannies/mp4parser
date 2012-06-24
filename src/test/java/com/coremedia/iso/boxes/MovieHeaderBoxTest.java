package com.coremedia.iso.boxes;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;

import java.util.Map;


public class MovieHeaderBoxTest extends BoxWriteReadBase<MovieHeaderBox> {
    @Override
    public Class<MovieHeaderBox> getBoxUnderTest() {
        return MovieHeaderBox.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere) {
        addPropsHere.put("creationTime", (long) 3453453345l);
        addPropsHere.put("currentTime", (int) 2342);
        addPropsHere.put("duration", (long) 243423);
        addPropsHere.put("matrix", (long[]) new long[]{1,2,3,4,5,6,7,8,9} );
        addPropsHere.put("modificationTime", (long) 423);
        addPropsHere.put("nextTrackId", (long)5543 );
        addPropsHere.put("posterTime", (int)5433 );
        addPropsHere.put("previewDuration", (int)5343 );
        addPropsHere.put("previewTime", (int)666 );
        addPropsHere.put("rate", (double) 1);
        addPropsHere.put("selectionDuration", (int) 32 );
        addPropsHere.put("selectionTime", (int)4456 );
        addPropsHere.put("timescale", (long)7565 );
        addPropsHere.put("volume", (float) 1.0);
    }
}
