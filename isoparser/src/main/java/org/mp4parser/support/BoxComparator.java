package org.mp4parser.support;

import org.junit.Assert;
import org.mp4parser.Box;
import org.mp4parser.Container;
import org.mp4parser.tools.Path;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.util.Iterator;

/**
 * Compares boxes for testing purposes.
 */
public class BoxComparator {


    public static boolean isIgnore(Container ref, Box b, String[] ignores) {
        for (String ignore : ignores) {
            if (Path.isContained(ref, b, ignore)) {
                return true;
            }
        }
        return false;
    }


    public static void check(Container root1, Box b1, Container root2, Box b2, String... ignores) throws IOException {
        //System.err.println(b1.getType() + " - " + b2.getType());
        Assert.assertEquals(b1.getType(), b2.getType());
        if (!isIgnore(root1, b1, ignores)) {
            //    System.err.println(b1.getType());
            Assert.assertEquals("Type differs. \ntypetrace ref : " + b1 + "\ntypetrace new : " + b2,
                    b1.getType(), b2.getType());
            if (b1 instanceof Container ^ !(b2 instanceof Container)) {
                if (b1 instanceof Container) {
                    check(root1, (Container) b1, root2, (Container) b2, ignores);
                } else {
                    checkBox(root1, b1, root2, b2, ignores);
                }
            } else {
                Assert.fail("Either both boxes are container boxes or none");
            }
        }
    }

    private static void checkBox(Container root1, Box b1, Container root2, Box b2, String[] ignores) throws IOException {
        if (!isIgnore(root1, b1, ignores)) {
            ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
            ByteArrayOutputStream baos2 = new ByteArrayOutputStream();

            b1.getBox(Channels.newChannel(baos1));
            b2.getBox(Channels.newChannel(baos2));

            baos1.close();
            baos2.close();

            Assert.assertArrayEquals("Box at " + b1 + " differs from reference\n\n" + b1.toString() + "\n" + b2.toString(), baos1.toByteArray(), baos2.toByteArray());
        }
    }

    public static void check(Container cb1, Container cb2, String... ignores) throws IOException {
        check(cb1, cb1, cb2, cb2, ignores);
    }


    public static void check(Container root1, Container cb1, Container root2, Container cb2, String... ignores) throws IOException {
        Iterator<Box> it1 = cb1.getBoxes().iterator();
        Iterator<Box> it2 = cb2.getBoxes().iterator();

        while (it1.hasNext() && it2.hasNext()) {
            Box b1 = it1.next();
            Box b2 = it2.next();

            check(root1, b1, root2, b2, ignores);
        }
        if (it1.hasNext()) {
            Assert.fail("There is a box missing in the current output of the tool: " + it1.next());
        }
        if (it2.hasNext()) {
            Assert.fail("There is a box too much in the current output of the tool: " + it2.next());
        }

    }


}
