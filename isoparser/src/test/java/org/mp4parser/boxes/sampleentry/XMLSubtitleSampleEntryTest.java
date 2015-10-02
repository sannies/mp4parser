package org.mp4parser.boxes.sampleentry;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;
import org.mp4parser.boxes.iso14496.part12.FreeBox;
import org.mp4parser.boxes.iso14496.part30.XMLSubtitleSampleEntry;

import java.util.Collections;
import java.util.Map;

/**
 * Created by sannies on 23.05.13.
 */
public class XMLSubtitleSampleEntryTest extends BoxWriteReadBase<XMLSubtitleSampleEntry> {
    @Override
    public Class<XMLSubtitleSampleEntry> getBoxUnderTest() {
        return XMLSubtitleSampleEntry.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, XMLSubtitleSampleEntry box) {
        addPropsHere.put("boxes", Collections.singletonList(new FreeBox(100)));
        addPropsHere.put("dataReferenceIndex", 12);
        addPropsHere.put("auxiliaryMimeTypes", "image/jpeg");
        addPropsHere.put("namespace", "urn:namespace:dunno");
        addPropsHere.put("schemaLocation", "here");
    }
}
