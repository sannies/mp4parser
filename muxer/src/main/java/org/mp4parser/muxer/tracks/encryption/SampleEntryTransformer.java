package org.mp4parser.muxer.tracks.encryption;

import org.mp4parser.boxes.sampleentry.SampleEntry;
import org.mp4parser.muxer.Sample;

import java.util.HashMap;

public interface SampleEntryTransformer {


    SampleEntry transform(SampleEntry se);
}
