package com.googlecode.mp4parser.boxes.cenc;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;

import java.util.Map;


public class ProtectionSystemSpecificHeaderBoxTest extends BoxWriteReadBase<ProtectionSystemSpecificHeaderBox> {

    @Override
    public Class<ProtectionSystemSpecificHeaderBox> getBoxUnderTest() {
        return ProtectionSystemSpecificHeaderBox.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, ProtectionSystemSpecificHeaderBox box) {
        addPropsHere.put("systemId", ProtectionSystemSpecificHeaderBox.OMA2_SYSTEM_ID);
        addPropsHere.put("content", new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 0});
    }
}
