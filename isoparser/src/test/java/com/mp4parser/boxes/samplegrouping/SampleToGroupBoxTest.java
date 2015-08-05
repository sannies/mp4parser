package com.mp4parser.boxes.samplegrouping;


import com.googlecode.mp4parser.boxes.BoxWriteReadBase;

import java.util.Arrays;
import java.util.Map;

public class SampleToGroupBoxTest extends BoxWriteReadBase<SampleToGroupBox> {
    @Override
    public Class<SampleToGroupBox> getBoxUnderTest() {
        return SampleToGroupBox.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, SampleToGroupBox box) {
        addPropsHere.put("entries", Arrays.asList(new SampleToGroupBox.Entry(1, 2), new SampleToGroupBox.Entry(2, 3), new SampleToGroupBox.Entry(10, 20)));
        addPropsHere.put("groupingType", "grp1");
        addPropsHere.put("groupingTypeParameter", "gtyp");
        addPropsHere.put("version", 1);

    }
}
