package com.coremedia.iso.boxes.sampleentry;

import com.coremedia.iso.boxes.FreeBox;
import com.googlecode.mp4parser.boxes.BoxWriteReadBase;

import java.util.Collections;
import java.util.Map;

/**
 * Created by sannies on 23.05.13.
 */
public class SubtitleSampleEntryTest extends BoxWriteReadBase<SubtitleSampleEntry> {
    @Override
    public Class<SubtitleSampleEntry> getBoxUnderTest() {
        return SubtitleSampleEntry.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, SubtitleSampleEntry box) {
        addPropsHere.put("boxes", Collections.singletonList(new FreeBox(100)));
        addPropsHere.put("dataReferenceIndex", 12);
        addPropsHere.put("imageMimeType", "image/jpeg");
        addPropsHere.put("namespace", "urn:namespace:dunno");
        addPropsHere.put("schemaLocation", "here");
    }
}
