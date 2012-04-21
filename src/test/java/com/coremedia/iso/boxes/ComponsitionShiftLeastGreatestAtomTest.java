package com.coremedia.iso.boxes;

import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.util.ByteBufferByteChannel;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
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
        CompositionShiftLeastGreatestAtom clsg = new CompositionShiftLeastGreatestAtom();
        clsg.setCompositionOffsetToDisplayOffsetShift(2);
        clsg.setDisplayEndTime(3);
        clsg.setDisplayStartTime(4);
        clsg.setGreatestDisplayOffset(-2);
        clsg.setLeastDisplayOffset(-4);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        clsg.getBox(Channels.newChannel(baos));
        byte[] content = baos.toByteArray();

        IsoFile isoFile = new IsoFile(new ByteBufferByteChannel(ByteBuffer.wrap(content)));

        CompositionShiftLeastGreatestAtom clsg2 = isoFile.getBoxes(CompositionShiftLeastGreatestAtom.class).get(0);
        Assert.assertEquals(content.length, clsg2.getSize());
        Assert.assertEquals(clsg.getCompositionOffsetToDisplayOffsetShift(), clsg2.getCompositionOffsetToDisplayOffsetShift());
        Assert.assertEquals(clsg.getGreatestDisplayOffset(), clsg2.getGreatestDisplayOffset());
        Assert.assertEquals(clsg.getDisplayEndTime(), clsg2.getDisplayEndTime());
        Assert.assertEquals(clsg.getDisplayStartTime(), clsg2.getDisplayStartTime());
        Assert.assertEquals(clsg.getLeastDisplayOffset(), clsg2.getLeastDisplayOffset());


    }
}
