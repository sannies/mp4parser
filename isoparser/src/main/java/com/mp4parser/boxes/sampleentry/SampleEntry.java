package com.mp4parser.boxes.sampleentry;

import com.mp4parser.ParsableBox;
import com.mp4parser.RandomAccessSource;

/**
 * Created by sannies on 30.05.13.
 */
public interface SampleEntry extends ParsableBox, RandomAccessSource.Container {
    int getDataReferenceIndex();
    void setDataReferenceIndex(int dataReferenceIndex);
}
