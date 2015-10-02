package org.mp4parser.boxes.sampleentry;

import org.mp4parser.Container;
import org.mp4parser.ParsableBox;

/**
 * Created by sannies on 30.05.13.
 */
public interface SampleEntry extends ParsableBox, Container {
    int getDataReferenceIndex();

    void setDataReferenceIndex(int dataReferenceIndex);
}
