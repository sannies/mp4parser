package com.mp4parser.tools.boxes;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;
import com.mp4parser.boxes.iso14496.part12.MediaHeaderBox;

import java.util.Date;
import java.util.Map;

/**
 * Created by sannies on 03.06.13.
 */
public class MediaHeaderBoxTest extends BoxWriteReadBase<MediaHeaderBox> {
    @Override
    public Class<MediaHeaderBox> getBoxUnderTest() {
        return MediaHeaderBox.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, MediaHeaderBox box) {
        addPropsHere.put("creationTime", new Date(1370253188000L));
        addPropsHere.put("duration", (long)12423 );
        addPropsHere.put("language", "ger");
        addPropsHere.put("modificationTime", new Date(1370253188000L) );
        addPropsHere.put("timescale", (long)24 );
    }
}
