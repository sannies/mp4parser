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
    public void setupProperties(Map<String, Object> values, SegmentIndexBox box) {
        values.put("referenceId", 726L);
        values.put("timeScale", 725L);
        values.put("earliestPresentationTime", 724L);
        values.put("firstOffset", 34567L);
        values.put("reserved", 0);
        values.put("entries", Arrays.asList(new SegmentIndexBox.Entry((byte) 1, 2, 3, true, (byte) 5, 6)));
    }
}
