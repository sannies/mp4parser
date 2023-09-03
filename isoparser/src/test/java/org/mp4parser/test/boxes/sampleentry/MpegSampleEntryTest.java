package org.mp4parser.test.boxes.sampleentry;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;
import org.mp4parser.boxes.iso14496.part12.FreeBox;
import org.mp4parser.boxes.sampleentry.MpegSampleEntry;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by sannies on 21.05.13.
 */
public class MpegSampleEntryTest extends BoxWriteReadBase<MpegSampleEntry> {
    @Override
    public Class<MpegSampleEntry> getBoxUnderTest() {
        return MpegSampleEntry.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, MpegSampleEntry box) {
        addPropsHere.put("boxes", (List) Collections.singletonList(new FreeBox(100)));
        addPropsHere.put("dataReferenceIndex", (int) 4344);
    }


}
