package org.mp4parser.muxer.tracks;

import org.junit.Assert;
import org.junit.Test;
import org.mp4parser.boxes.iso14496.part12.CompositionTimeToSample;
import org.mp4parser.boxes.iso14496.part12.TimeToSampleBox;

import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: sannies
 * Date: 10/28/12
 * Time: 1:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClippedTrackTest {
    @Test
    public void testGetDecodingTimeEntries() throws Exception {
        LinkedList<TimeToSampleBox.Entry> e = new LinkedList<TimeToSampleBox.Entry>();
        e.add(new TimeToSampleBox.Entry(2, 3));
        e.add(new TimeToSampleBox.Entry(3, 4));
        e.add(new TimeToSampleBox.Entry(3, 5));
        e.add(new TimeToSampleBox.Entry(2, 6));
        List<TimeToSampleBox.Entry> r = ClippedTrack.getDecodingTimeEntries(e, 0, 1);
        Assert.assertEquals(1, r.size());
        Assert.assertEquals(1, r.get(0).getCount());
        Assert.assertEquals(3, r.get(0).getDelta());
        r = ClippedTrack.getDecodingTimeEntries(e, 0, 2);
        Assert.assertEquals(1, r.size());
        Assert.assertEquals(2, r.get(0).getCount());
        Assert.assertEquals(3, r.get(0).getDelta());
        r = ClippedTrack.getDecodingTimeEntries(e, 1, 2);
        Assert.assertEquals(1, r.size());
        Assert.assertEquals(1, r.get(0).getCount());
        Assert.assertEquals(3, r.get(0).getDelta());
        r = ClippedTrack.getDecodingTimeEntries(e, 1, 3);
        Assert.assertEquals(2, r.size());
        Assert.assertEquals(1, r.get(0).getCount());
        Assert.assertEquals(3, r.get(0).getDelta());
        Assert.assertEquals(1, r.get(1).getCount());
        Assert.assertEquals(4, r.get(1).getDelta());
        r = ClippedTrack.getDecodingTimeEntries(e, 3, 4);
        Assert.assertEquals(1, r.size());
        Assert.assertEquals(1, r.get(0).getCount());
        Assert.assertEquals(4, r.get(0).getDelta());
        r = ClippedTrack.getDecodingTimeEntries(e, 1, 6);
        Assert.assertEquals(3, r.size());
        Assert.assertEquals(1, r.get(0).getCount());
        Assert.assertEquals(3, r.get(0).getDelta());
        Assert.assertEquals(3, r.get(1).getCount());
        Assert.assertEquals(4, r.get(1).getDelta());
        Assert.assertEquals(1, r.get(2).getCount());
        Assert.assertEquals(5, r.get(2).getDelta());
        r = ClippedTrack.getDecodingTimeEntries(e, 2, 6);
        Assert.assertEquals(2, r.size());
        Assert.assertEquals(3, r.get(0).getCount());
        Assert.assertEquals(4, r.get(0).getDelta());
        Assert.assertEquals(1, r.get(1).getCount());
        Assert.assertEquals(5, r.get(1).getDelta());
        r = ClippedTrack.getDecodingTimeEntries(e, 2, 8);
        Assert.assertEquals(2, r.size());
        Assert.assertEquals(3, r.get(0).getCount());
        Assert.assertEquals(4, r.get(0).getDelta());
        Assert.assertEquals(3, r.get(1).getCount());
        Assert.assertEquals(5, r.get(1).getDelta());

    }

    @Test
    public void testGetCompositionTimes() throws Exception {
        LinkedList<CompositionTimeToSample.Entry> e = new LinkedList<CompositionTimeToSample.Entry>();
        e.add(new CompositionTimeToSample.Entry(2, 3));
        e.add(new CompositionTimeToSample.Entry(3, 4));
        e.add(new CompositionTimeToSample.Entry(3, 5));
        e.add(new CompositionTimeToSample.Entry(2, 6));
        List<CompositionTimeToSample.Entry> r = ClippedTrack.getCompositionTimeEntries(e, 0, 1);
        Assert.assertEquals(1, r.size());
        Assert.assertEquals(1, r.get(0).getCount());
        Assert.assertEquals(3, r.get(0).getOffset());
        r = ClippedTrack.getCompositionTimeEntries(e, 0, 2);
        Assert.assertEquals(1, r.size());
        Assert.assertEquals(2, r.get(0).getCount());
        Assert.assertEquals(3, r.get(0).getOffset());
        r = ClippedTrack.getCompositionTimeEntries(e, 1, 2);
        Assert.assertEquals(1, r.size());
        Assert.assertEquals(1, r.get(0).getCount());
        Assert.assertEquals(3, r.get(0).getOffset());
        r = ClippedTrack.getCompositionTimeEntries(e, 1, 3);
        Assert.assertEquals(2, r.size());
        Assert.assertEquals(1, r.get(0).getCount());
        Assert.assertEquals(3, r.get(0).getOffset());
        Assert.assertEquals(1, r.get(1).getCount());
        Assert.assertEquals(4, r.get(1).getOffset());
        r = ClippedTrack.getCompositionTimeEntries(e, 3, 4);
        Assert.assertEquals(1, r.size());
        Assert.assertEquals(1, r.get(0).getCount());
        Assert.assertEquals(4, r.get(0).getOffset());
        r = ClippedTrack.getCompositionTimeEntries(e, 1, 6);
        Assert.assertEquals(3, r.size());
        Assert.assertEquals(1, r.get(0).getCount());
        Assert.assertEquals(3, r.get(0).getOffset());
        Assert.assertEquals(3, r.get(1).getCount());
        Assert.assertEquals(4, r.get(1).getOffset());
        Assert.assertEquals(1, r.get(2).getCount());
        Assert.assertEquals(5, r.get(2).getOffset());
        r = ClippedTrack.getCompositionTimeEntries(e, 2, 6);
        Assert.assertEquals(2, r.size());
        Assert.assertEquals(3, r.get(0).getCount());
        Assert.assertEquals(4, r.get(0).getOffset());
        Assert.assertEquals(1, r.get(1).getCount());
        Assert.assertEquals(5, r.get(1).getOffset());
        r = ClippedTrack.getCompositionTimeEntries(e, 2, 8);
        Assert.assertEquals(2, r.size());
        Assert.assertEquals(3, r.get(0).getCount());
        Assert.assertEquals(4, r.get(0).getOffset());
        Assert.assertEquals(3, r.get(1).getCount());
        Assert.assertEquals(5, r.get(1).getOffset());
    }
}
