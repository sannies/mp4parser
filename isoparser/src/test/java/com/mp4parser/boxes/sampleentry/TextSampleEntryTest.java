package com.mp4parser.boxes.sampleentry;

import com.mp4parser.boxes.iso14496.part12.FreeBox;
import com.googlecode.mp4parser.boxes.BoxWriteReadBase;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

public class TextSampleEntryTest extends BoxWriteReadBase<TextSampleEntry> {

    @Test
    public void testBitSetters() {
        TextSampleEntry tx3g = new TextSampleEntry();
        tx3g.setContinuousKaraoke(true);
        Assert.assertTrue(tx3g.isContinuousKaraoke());
        tx3g.setContinuousKaraoke(false);
        Assert.assertFalse(tx3g.isContinuousKaraoke());
    }

    @Override
    public Class<TextSampleEntry> getBoxUnderTest() {
        return TextSampleEntry.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, TextSampleEntry box) {
        addPropsHere.put("backgroundColorRgba", new int[]{1, 2, 3, 4});
        addPropsHere.put("boxRecord", new TextSampleEntry.BoxRecord(31, 41, 51, 61));
        addPropsHere.put("boxes", Collections.singletonList(new FreeBox(100)));
        addPropsHere.put("continuousKaraoke", true);
        addPropsHere.put("dataReferenceIndex", 4);
        addPropsHere.put("fillTextRegion", true);
        addPropsHere.put("horizontalJustification", 20);
        addPropsHere.put("scrollDirection", false);
        addPropsHere.put("scrollIn", false);
        addPropsHere.put("scrollOut", true);
        addPropsHere.put("styleRecord", new TextSampleEntry.StyleRecord(7, 8, 9, 10, 11, new int[]{0xfe, 0xfd, 0xfc, 0xfb}));
        addPropsHere.put("verticalJustification", 43);
        addPropsHere.put("writeTextVertically", true);
    }
}
