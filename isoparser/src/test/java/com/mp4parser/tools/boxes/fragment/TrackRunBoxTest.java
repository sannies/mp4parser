package com.mp4parser.tools.boxes.fragment;

import com.mp4parser.IsoFile;
import com.mp4parser.boxes.iso14496.part12.SampleFlags;
import com.mp4parser.boxes.iso14496.part12.TrackRunBox;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

public class TrackRunBoxTest {


    @Test
    public void test() throws IOException {
        testAllFlagsWithDataOffset(new SampleFlags(ByteBuffer.wrap(new byte[]{32, 3, 65, 127})));
        testAllFlagsWithDataOffset(null);
    }

    public void testAllFlagsWithDataOffset(SampleFlags sf) throws IOException {
        testAllFlags(-1, sf);
        testAllFlags(1000, sf);
    }

    public void testAllFlags(int dataOffset, SampleFlags sf) throws IOException {
        simpleRoundTrip(false, false, false, dataOffset, sf);
        simpleRoundTrip(false, false, true, dataOffset, sf);
        simpleRoundTrip(false, true, false, dataOffset, sf);
        simpleRoundTrip(false, true, true, dataOffset, sf);
        simpleRoundTrip(true, false, false, dataOffset, sf);
        simpleRoundTrip(true, false, true, dataOffset, sf);
        simpleRoundTrip(true, true, false, dataOffset, sf);
        simpleRoundTrip(true, true, true, dataOffset, sf);
    }

    public void simpleRoundTrip(boolean isSampleSizePresent,
                                boolean isSampleDurationPresent,
                                boolean isSampleCompositionTimeOffsetPresent,
                                int dataOffset, SampleFlags sf) throws IOException {
        TrackRunBox trun = new TrackRunBox();
        trun.setFirstSampleFlags(sf);
        trun.setSampleSizePresent(!isSampleSizePresent);
        trun.setSampleSizePresent(isSampleSizePresent);
        trun.setSampleDurationPresent(!isSampleDurationPresent);
        trun.setSampleDurationPresent(isSampleDurationPresent);
        trun.setSampleCompositionTimeOffsetPresent(!isSampleCompositionTimeOffsetPresent);
        trun.setSampleCompositionTimeOffsetPresent(isSampleCompositionTimeOffsetPresent);
        trun.setDataOffset(dataOffset);
        List<TrackRunBox.Entry> entries = new LinkedList<TrackRunBox.Entry>();
        entries.add(new TrackRunBox.Entry(1000, 2000, new SampleFlags(), 3000));
        entries.add(new TrackRunBox.Entry(1001, 2001, new SampleFlags(), 3001));
        trun.setEntries(entries);

        File f = File.createTempFile(this.getClass().getSimpleName(), "");
        f.deleteOnExit();
        FileChannel fc = new FileOutputStream(f).getChannel();
        trun.getBox(fc);
        fc.close();


        IsoFile isoFile = new IsoFile(new FileInputStream(f).getChannel());
        TrackRunBox trun2 = (TrackRunBox) isoFile.getBoxes().get(0);

        Assert.assertEquals(trun.isDataOffsetPresent(), trun2.isDataOffsetPresent());
        Assert.assertEquals(trun.isSampleCompositionTimeOffsetPresent(), trun2.isSampleCompositionTimeOffsetPresent());
        Assert.assertEquals(trun.isSampleDurationPresent(), trun2.isSampleDurationPresent());
        Assert.assertEquals(trun.isSampleFlagsPresent(), trun2.isSampleFlagsPresent());
        Assert.assertEquals(trun.isSampleSizePresent(), trun2.isSampleSizePresent());
        Assert.assertEquals(trun.getDataOffset(), trun2.getDataOffset());

        Assert.assertEquals(trun.getDataOffset(), trun2.getDataOffset());
        Assert.assertEquals(trun.getFirstSampleFlags(), trun2.getFirstSampleFlags());


    }

}
