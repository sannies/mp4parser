package com.coremedia.iso.boxes;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class ProgressiveDownloadInformationBoxTest extends BoxWriteReadBase<ProgressiveDownloadInformationBox> {
    @Override
    public Class<ProgressiveDownloadInformationBox> getBoxUnderTest() {
        return ProgressiveDownloadInformationBox.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, ProgressiveDownloadInformationBox box) {
        List<ProgressiveDownloadInformationBox.Entry> entries = new LinkedList<ProgressiveDownloadInformationBox.Entry>();
        entries.add(new ProgressiveDownloadInformationBox.Entry(10, 20));
        entries.add(new ProgressiveDownloadInformationBox.Entry(20, 10));
        addPropsHere.put("entries", entries);
    }
}
