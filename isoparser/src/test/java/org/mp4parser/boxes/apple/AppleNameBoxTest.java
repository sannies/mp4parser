package org.mp4parser.boxes.apple;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;

import java.util.Map;

/**
 * Created by sannies on 10/15/13.
 */
public class AppleNameBoxTest extends BoxWriteReadBase<AppleNameBox> {
    @Override
    public Class<AppleNameBox> getBoxUnderTest() {
        return AppleNameBox.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, AppleNameBox box) {
        addPropsHere.put("value", "The Arrangement");
    }
}
