package com.coremedia.iso.boxes.sampleentry;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;

import java.util.Map;

/**
 * Created by sannies on 22.05.13.
 */
public class Ovc1VisualSampleEntryImplTest extends BoxWriteReadBase<Ovc1VisualSampleEntryImpl> {
    @Override
    public Class<Ovc1VisualSampleEntryImpl> getBoxUnderTest() {
        return Ovc1VisualSampleEntryImpl.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, Ovc1VisualSampleEntryImpl box) {
        addPropsHere.put("dataReferenceIndex", (int) 546);
        addPropsHere.put("vc1Content", (byte[]) new byte[]{1, 2, 3, 4, 5, 6, 1, 2, 3});
    }
}
