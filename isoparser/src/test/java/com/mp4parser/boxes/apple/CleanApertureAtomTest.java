package com.mp4parser.boxes.apple;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;

import java.util.Map;


public class CleanApertureAtomTest extends BoxWriteReadBase<CleanApertureAtom> {
    @Override
    public Class<CleanApertureAtom> getBoxUnderTest() {
        return CleanApertureAtom.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, CleanApertureAtom box) {
        addPropsHere.put("height", 123.0);
        addPropsHere.put("width", 321.0);
    }
}
