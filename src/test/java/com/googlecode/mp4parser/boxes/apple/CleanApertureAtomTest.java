package com.googlecode.mp4parser.boxes.apple;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;

import java.util.Map;


public class CleanApertureAtomTest extends BoxWriteReadBase<CleanApertureAtom> {
    @Override
    public Class<CleanApertureAtom> getBoxUnderTest() {
        return CleanApertureAtom.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere) {
        addPropsHere.put("height", (long) 123);
        addPropsHere.put("width", (long) 321);
    }
}
