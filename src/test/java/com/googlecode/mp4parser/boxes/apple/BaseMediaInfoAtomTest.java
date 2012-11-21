package com.googlecode.mp4parser.boxes.apple;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: sannies
 * Date: 6/24/12
 * Time: 3:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class BaseMediaInfoAtomTest extends BoxWriteReadBase<BaseMediaInfoAtom> {
    @Override
    public Class<BaseMediaInfoAtom> getBoxUnderTest() {
        return BaseMediaInfoAtom.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, BaseMediaInfoAtom box) {
        addPropsHere.put("balance", (short) 321);
        addPropsHere.put("graphicsMode", (short) 43);
        addPropsHere.put("opColorB", (int) 124);
        addPropsHere.put("opColorG", (int) 445);
        addPropsHere.put("opColorR", (int) 5321);
        addPropsHere.put("reserved", (short) 344);
    }
}
