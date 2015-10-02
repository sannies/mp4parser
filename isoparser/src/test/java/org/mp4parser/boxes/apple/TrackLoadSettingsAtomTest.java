package org.mp4parser.boxes.apple;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: sannies2
 * Date: 2/1/13
 * Time: 11:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class TrackLoadSettingsAtomTest extends BoxWriteReadBase<TrackLoadSettingsAtom> {
    @Override
    public Class<TrackLoadSettingsAtom> getBoxUnderTest() {
        return TrackLoadSettingsAtom.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, TrackLoadSettingsAtom box) {
        addPropsHere.put("defaultHints", 34);
        addPropsHere.put("preloadDuration", 35);
        addPropsHere.put("preloadFlags", 36);
        addPropsHere.put("preloadStartTime", 37);

    }
}
