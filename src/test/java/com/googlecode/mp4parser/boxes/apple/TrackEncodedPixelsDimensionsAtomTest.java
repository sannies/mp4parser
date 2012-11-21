package com.googlecode.mp4parser.boxes.apple;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: sannies
 * Date: 11/18/12
 * Time: 11:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class TrackEncodedPixelsDimensionsAtomTest extends BoxWriteReadBase<TrackEncodedPixelsDimensionsAtom> {
    @Override
    public Class<TrackEncodedPixelsDimensionsAtom> getBoxUnderTest() {
        return TrackEncodedPixelsDimensionsAtom.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, TrackEncodedPixelsDimensionsAtom box) {
        addPropsHere.put("height", 123.0);
        addPropsHere.put("width", 321.0);
    }
}

