package com.mp4parser.tools.boxes;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;
import com.mp4parser.ParsableBox;
import com.mp4parser.boxes.iso14496.part12.FreeBox;
import com.mp4parser.boxes.iso14496.part12.UserDataBox;

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
        addPropsHere.put("boxes", Arrays.asList((ParsableBox) new FreeBox(100), new FreeBox(200)));
    }
}
