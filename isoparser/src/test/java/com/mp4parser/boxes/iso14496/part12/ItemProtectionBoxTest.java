package com.mp4parser.boxes.iso14496.part12;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;
import com.mp4parser.boxes.iso14496.part12.FreeBox;
import com.mp4parser.boxes.iso14496.part12.ItemProtectionBox;

import java.util.Collections;
import java.util.Map;

/**
 * Created by sannies on 26.05.13.
 */
public class ItemProtectionBoxTest extends BoxWriteReadBase<ItemProtectionBox> {
    @Override
    public Class<ItemProtectionBox> getBoxUnderTest() {
        return ItemProtectionBox.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, ItemProtectionBox box) {
        addPropsHere.put("boxes", Collections.singletonList(new FreeBox(1000)));
    }
}
