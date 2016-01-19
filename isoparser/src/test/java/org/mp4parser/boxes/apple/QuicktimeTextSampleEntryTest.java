package org.mp4parser.boxes.apple;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;

import java.util.Map;

public class QuicktimeTextSampleEntryTest extends BoxWriteReadBase<QuicktimeTextSampleEntry> {
    public QuicktimeTextSampleEntryTest() {
        super("stsd");
    }

    @Override
    public Class<QuicktimeTextSampleEntry> getBoxUnderTest() {
        return QuicktimeTextSampleEntry.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, QuicktimeTextSampleEntry box) {
        addPropsHere.put("backgroundB", 5);
        addPropsHere.put("backgroundG", 10);
        addPropsHere.put("backgroundR", 15);
        addPropsHere.put("dataReferenceIndex", 1);
        addPropsHere.put("defaultTextBox", 54634562222l);
        addPropsHere.put("displayFlags", 324);
        addPropsHere.put("reserved1", (long) 0);
        addPropsHere.put("textJustification", 1);
        addPropsHere.put("fontFace", (short) 0);
        addPropsHere.put("fontName", "45uku");
        addPropsHere.put("fontNumber", (short) 0);
        addPropsHere.put("foregroundB", 115);
        addPropsHere.put("foregroundG", 120);
        addPropsHere.put("foregroundR", 125);
        addPropsHere.put("reserved2", (byte) 0);
        addPropsHere.put("reserved3", (short) 0);
    }
}
