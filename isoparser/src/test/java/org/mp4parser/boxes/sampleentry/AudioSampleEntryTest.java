package org.mp4parser.boxes.sampleentry;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;
import org.mp4parser.boxes.iso14496.part12.FreeBox;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by sannies on 22.05.13.
 */
public class AudioSampleEntryTest extends BoxWriteReadBase<AudioSampleEntry> {

    @Override
    protected AudioSampleEntry getInstance(Class<AudioSampleEntry> clazz) throws Exception {
        return new AudioSampleEntry(AudioSampleEntry.TYPE2);
    }

    @Override
    public Class<AudioSampleEntry> getBoxUnderTest() {
        return AudioSampleEntry.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, AudioSampleEntry box) {
        addPropsHere.put("boxes", (List) Collections.singletonList(new FreeBox(100)));
        addPropsHere.put("bytesPerFrame", (long) 1);
        addPropsHere.put("bytesPerPacket", (long) 2);
        addPropsHere.put("bytesPerSample", (long) 3);
        addPropsHere.put("channelCount", 4);
        addPropsHere.put("compressionId", 5);
        addPropsHere.put("dataReferenceIndex", 7);
        addPropsHere.put("packetSize", 8);
        addPropsHere.put("reserved1", 9);
        addPropsHere.put("reserved2", (long) 10);
        addPropsHere.put("sampleRate", (long) 11);
        addPropsHere.put("sampleSize", 12);
        addPropsHere.put("samplesPerPacket", (long) 13);
        addPropsHere.put("soundVersion", 1);
        addPropsHere.put("soundVersion2Data", null);
    }
}
