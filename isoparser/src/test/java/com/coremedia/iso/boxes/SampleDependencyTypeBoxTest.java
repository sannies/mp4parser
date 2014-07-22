package com.coremedia.iso.boxes;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class SampleDependencyTypeBoxTest extends BoxWriteReadBase<SampleDependencyTypeBox> {


    @Override
    public Class<SampleDependencyTypeBox> getBoxUnderTest() {
        return SampleDependencyTypeBox.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, SampleDependencyTypeBox box) {
        List<SampleDependencyTypeBox.Entry> l = new LinkedList<SampleDependencyTypeBox.Entry>();
        for (int i = 0; i < 0xcf; i++) {
            SampleDependencyTypeBox.Entry e = new SampleDependencyTypeBox.Entry(i);
            l.add(e);
        }
        addPropsHere.put("entries", l);
    }
}
