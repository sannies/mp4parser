package com.googlecode.mp4parser.boxes.apple;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: sannies
 * Date: 11/18/12
 * Time: 11:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class TrackProductionApertureDimensionsAtomTest extends BoxWriteReadBase<TrackProductionApertureDimensionsAtom> {
    @Override
    public Class<TrackProductionApertureDimensionsAtom> getBoxUnderTest() {
        return TrackProductionApertureDimensionsAtom.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, TrackProductionApertureDimensionsAtom box) {
        addPropsHere.put("height", 123.0);
        addPropsHere.put("width", 321.0);
    }
}
