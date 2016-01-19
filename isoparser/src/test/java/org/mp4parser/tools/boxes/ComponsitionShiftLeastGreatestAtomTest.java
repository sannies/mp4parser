package org.mp4parser.tools.boxes;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.mp4parser.IsoFile;
import org.mp4parser.boxes.iso14496.part12.CompositionToDecodeBox;
import org.mp4parser.tools.ByteBufferByteChannel;

import java.io.ByteArrayOutputStream;
import java.nio.channels.Channels;

/**
 * Created by IntelliJ IDEA.
 * User: sannies
 * Date: 24.02.11
 * Time: 12:41
 * To change this template use File | Settings | File Templates.
 */
public class ComponsitionShiftLeastGreatestAtomTest extends TestCase {


    public void testParse() throws Exception {
        CompositionToDecodeBox clsg = new CompositionToDecodeBox();
        clsg.setCompositionOffsetToDisplayOffsetShift(2);
        clsg.setDisplayEndTime(3);
        clsg.setDisplayStartTime(4);
        clsg.setGreatestDisplayOffset(-2);
        clsg.setLeastDisplayOffset(-4);


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        clsg.getBox(Channels.newChannel(baos));
        IsoFile isoFile = new IsoFile(new ByteBufferByteChannel(baos.toByteArray()));

        CompositionToDecodeBox clsg2 = isoFile.getBoxes(CompositionToDecodeBox.class).get(0);
        Assert.assertEquals(baos.toByteArray().length, clsg2.getSize());
        Assert.assertEquals(clsg.getCompositionOffsetToDisplayOffsetShift(), clsg2.getCompositionOffsetToDisplayOffsetShift());
        Assert.assertEquals(clsg.getGreatestDisplayOffset(), clsg2.getGreatestDisplayOffset());
        Assert.assertEquals(clsg.getDisplayEndTime(), clsg2.getDisplayEndTime());
        Assert.assertEquals(clsg.getDisplayStartTime(), clsg2.getDisplayStartTime());
        Assert.assertEquals(clsg.getLeastDisplayOffset(), clsg2.getLeastDisplayOffset());


    }
}
