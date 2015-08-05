package com.mp4parser.boxes.apple;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: sannies
 * Date: 6/24/12
 * Time: 4:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class GenericMediaHeaderTextAtomTest extends BoxWriteReadBase<GenericMediaHeaderTextAtom> {
    public GenericMediaHeaderTextAtomTest() {
        super("gmhd");
    }

    @Override
    public Class<GenericMediaHeaderTextAtom> getBoxUnderTest() {
        return GenericMediaHeaderTextAtom.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, GenericMediaHeaderTextAtom box) {
        addPropsHere.put("unknown_1", (int) 1);
        addPropsHere.put("unknown_2", (int) 2);
        addPropsHere.put("unknown_3", (int) 3);
        addPropsHere.put("unknown_4", (int) 4);
        addPropsHere.put("unknown_5", (int) 5);
        addPropsHere.put("unknown_6", (int) 6);
        addPropsHere.put("unknown_7", (int) 7);
        addPropsHere.put("unknown_8", (int) 8);
        addPropsHere.put("unknown_9", (int) 9);
    }
}
