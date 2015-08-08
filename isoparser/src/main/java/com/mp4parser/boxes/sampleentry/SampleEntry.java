package com.mp4parser.boxes.sampleentry;

import com.mp4parser.Container;
import com.mp4parser.ParsableBox;

/**
 * Created by sannies on 30.05.13.
 */
public interface SampleEntry extends ParsableBox, Container {
    int getDataReferenceIndex();
    void setDataReferenceIndex(int dataReferenceIndex);
}
