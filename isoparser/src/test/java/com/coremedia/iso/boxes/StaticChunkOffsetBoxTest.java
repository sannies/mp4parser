package com.coremedia.iso.boxes;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;

import java.util.Map;

/**
 * Created by sannies on 30.05.13.
 */
public class StaticChunkOffsetBoxTest extends BoxWriteReadBase<StaticChunkOffsetBox> {
    @Override
    public Class<StaticChunkOffsetBox> getBoxUnderTest() {
        return StaticChunkOffsetBox.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, StaticChunkOffsetBox box) {
        addPropsHere.put("chunkOffsets", new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 0});
    }
}
