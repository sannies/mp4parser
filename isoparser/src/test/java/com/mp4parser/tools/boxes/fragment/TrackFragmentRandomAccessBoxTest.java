package com.mp4parser.tools.boxes.fragment;


import com.mp4parser.IsoFile;
import com.mp4parser.boxes.iso14496.part12.TrackFragmentRandomAccessBox;
import junit.framework.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

public class TrackFragmentRandomAccessBoxTest {
    @Test
    public void testRoundtrip() throws IOException {
        testRoundtrip(1, 1, 1);
        testRoundtrip(2, 1, 1);
        testRoundtrip(4, 1, 1);
        testRoundtrip(1, 2, 1);
        testRoundtrip(2, 2, 1);
        testRoundtrip(4, 2, 1);
        testRoundtrip(1, 4, 1);
        testRoundtrip(2, 4, 1);
        testRoundtrip(4, 4, 1);

        testRoundtrip(1, 1, 2);
        testRoundtrip(2, 1, 2);
        testRoundtrip(4, 1, 2);
        testRoundtrip(1, 2, 2);
        testRoundtrip(2, 2, 2);
        testRoundtrip(4, 2, 2);
        testRoundtrip(1, 4, 2);
        testRoundtrip(2, 4, 2);
        testRoundtrip(4, 4, 2);

        testRoundtrip(1, 1, 4);
        testRoundtrip(2, 1, 4);
        testRoundtrip(4, 1, 4);
        testRoundtrip(1, 2, 4);
        testRoundtrip(2, 2, 4);
        testRoundtrip(4, 2, 4);
        testRoundtrip(1, 4, 4);
        testRoundtrip(2, 4, 4);
        testRoundtrip(4, 4, 4);
    }

    public void testRoundtrip(int sizeOfSampleNum, int lengthSizeOfTrafNum, int lengthSizeOfTrunNum) throws IOException {
        TrackFragmentRandomAccessBox traf = new TrackFragmentRandomAccessBox();
        traf.setLengthSizeOfSampleNum(sizeOfSampleNum);
        traf.setLengthSizeOfTrafNum(lengthSizeOfTrafNum);
        traf.setLengthSizeOfTrunNum(lengthSizeOfTrunNum);
        List<TrackFragmentRandomAccessBox.Entry> entries = new LinkedList<TrackFragmentRandomAccessBox.Entry>();
        entries.add(new TrackFragmentRandomAccessBox.Entry(1, 2, 3, 4, 5));

        traf.setEntries(entries);

        File f = File.createTempFile(this.getClass().getSimpleName(), "");
        f.deleteOnExit();
        FileChannel fc = new FileOutputStream(f).getChannel();
        traf.getBox(fc);
        fc.close();


        IsoFile isoFile = new IsoFile(new FileInputStream(f).getChannel());
        TrackFragmentRandomAccessBox traf2 = (TrackFragmentRandomAccessBox) isoFile.getBoxes().get(0);
        Assert.assertEquals(traf.getNumberOfEntries(), traf2.getNumberOfEntries());
        Assert.assertEquals(traf.getReserved(), traf2.getReserved());
        Assert.assertEquals(traf.getTrackId(), traf2.getTrackId());
        //System.err.println("" + sizeOfSampleNum + " " + lengthSizeOfTrafNum + " " + lengthSizeOfTrunNum);
        Assert.assertEquals(traf.getEntries(), traf2.getEntries());

    }

}
