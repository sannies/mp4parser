package com.googlecode.mp4parser.boxes.ultraviolet;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;
import com.googlecode.mp4parser.boxes.dece.BaseLocationBox;

import java.util.Map;

/**
 *
 */
public class BaseLocationBoxTest extends BoxWriteReadBase<BaseLocationBox> {


    @Override
    public Class<BaseLocationBox> getBoxUnderTest() {
        return BaseLocationBox.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, BaseLocationBox box) {
        addPropsHere.put("baseLocation", " ");
        addPropsHere.put("purchaseLocation", " ");
    }
}
