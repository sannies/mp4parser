package com.coremedia.iso.boxes.sampleentry;

import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.Container;

/**
 * Created by sannies on 30.05.13.
 */
public interface SampleEntry extends Box, Container {
    int getDataReferenceIndex();
    void setDataReferenceIndex(int dataReferenceIndex);
}
