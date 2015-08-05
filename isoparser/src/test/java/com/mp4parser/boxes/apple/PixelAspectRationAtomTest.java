package com.mp4parser.boxes.apple;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;

import java.util.Map;

public class PixelAspectRationAtomTest extends BoxWriteReadBase<PixelAspectRationAtom> {
    @Override
    public Class<PixelAspectRationAtom> getBoxUnderTest() {
        return PixelAspectRationAtom.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, PixelAspectRationAtom box) {
        addPropsHere.put("hSpacing", 25);
        addPropsHere.put("vSpacing", 26);
    }
}
