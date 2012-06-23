package com.googlecode.mp4parser.boxes.threegpp26244;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;

import java.util.Arrays;
import java.util.Map;


public class SegmentIndexBoxTest extends BoxWriteReadBase<SegmentIndexBox> {
    @Override
    public Class<SegmentIndexBox> getBoxUnderTest() {
        return SegmentIndexBox.class;
    }

    @Override
    public void setupProperties(Map<String, Object> values) {
        values.put("referenceId", 726);
        values.put("timeScale", 725);
        values.put("earliestPresentationTime", 724);
        values.put("firstOffset", 34567);
        values.put("reserved", 0);
        values.put("entries", Arrays.asList(new SegmentIndexBox.Entry((byte) 1, 2, 3, (byte) 1, (byte) 5, 6)));
    }
}
