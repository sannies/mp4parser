package com.coremedia.iso.boxes;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;

import java.util.Collections;
import java.util.Map;

/**
 * Created by sannies on 25.05.13.
 */
public class SampleDescriptionBoxTest extends BoxWriteReadBase<SampleDescriptionBox> {
    @Override
    public Class<SampleDescriptionBox> getBoxUnderTest() {
        return SampleDescriptionBox.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, SampleDescriptionBox box) {
        addPropsHere.put("boxes", Collections.singletonList(new FreeBox(100)));
    }
}
