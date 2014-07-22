package com.coremedia.iso.boxes;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;

import java.util.Arrays;
import java.util.Map;

/**
 * Created by sannies on 26.05.13.
 */
public class UserDataBoxTest extends BoxWriteReadBase<UserDataBox> {
    @Override
    public Class<UserDataBox> getBoxUnderTest() {
        return UserDataBox.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, UserDataBox box) {
        addPropsHere.put("boxes", Arrays.asList((Box) new FreeBox(100), new FreeBox(200)));
    }
}
