package com.googlecode.mp4parser.authoring.tracks;

import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ContainerBox;
import com.googlecode.mp4parser.util.Path;
import org.junit.Assert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Compares boxes for testing purposes.
 */
public class BoxComparator {


    public static boolean isIgnore(Box b, List<String> ignores) {
        for (String ignore : ignores) {
            if (Path.isContained(b, ignore)) {
                return true;
            }
        }
        return false;
    }

    public static void check(Box b1, Box b2, List<String> ignores) throws IOException {
        Assert.assertEquals(b1.getType(), b2.getType());
        if (!isIgnore(b1, ignores)) {
        //    System.err.println(b1.getType());
            Assert.assertEquals("Type differs. \ntypetrace ref : " + Path.createPath(b1) + "\ntypetrace new : " + Path.createPath(b2),
                    b1.getType(), b2.getType());
            if (b1 instanceof ContainerBox ^ !(b2 instanceof ContainerBox)) {
                if (b1 instanceof ContainerBox) {
                    checkContainer((ContainerBox) b1, (ContainerBox) b2, ignores);
                } else {
                    chechBox(b1, b2, ignores);
                }
            } else {
                Assert.fail("Either both boxes are container boxes or none");
            }
        }
    }

    private static void chechBox(Box b1, Box b2, List<String> ignores) throws IOException {
        if (!isIgnore(b1, ignores)) {
            ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
            ByteArrayOutputStream baos2 = new ByteArrayOutputStream();

            b1.getBox(Channels.newChannel(baos1));
            b2.getBox(Channels.newChannel(baos2));

            baos1.close();
            baos2.close();

            Assert.assertArrayEquals("Box at " + Path.createPath(b1) + " differs from reference\n\n" + b1.toString() + "\n" + b2.toString(), baos1.toByteArray(), baos2.toByteArray());
        }
    }

    private static void checkContainer(ContainerBox cb1, ContainerBox cb2, List<String> ignores) throws IOException {
        Iterator<Box> it1 = cb1.getBoxes().iterator();
        Iterator<Box> it2 = cb2.getBoxes().iterator();

        while (it1.hasNext() && it2.hasNext()) {
            Box b1 = it1.next();
            Box b2 = it2.next();

            check(b1, b2, ignores);
        }
        if (it1.hasNext()) {
            Assert.fail("There is a box missing in the current output of the tool: " + it1.next());
        }
        if (it2.hasNext()) {
            Assert.fail("There is a box too much in the current output of the tool: " + it2.next());
        }

    }


}
