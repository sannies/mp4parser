package com.googlecode.mp4parser.authoring.tracks;

import org.junit.Assert;
import org.junit.Test;

import static com.mp4parser.authoring.tracks.ttml.TtmlHelpers.toTime;
import static com.mp4parser.authoring.tracks.ttml.TtmlHelpers.toTimeExpression;

/**
 * Created by sannies on 06.08.2015.
 */
public class TtmlHelpersTest {
    @Test
    public void testToTime() throws Exception {
        Assert.assertEquals(-3599000, toTime("-00:59:59.000"));
        Assert.assertEquals(3599000, toTime("00:59:59.000"));
    }

    @Test
    public void testToTimeExpression() throws Exception {
        Assert.assertEquals("-00:59:59.009", toTimeExpression(-3599009));
        Assert.assertEquals("00:59:59.010", toTimeExpression(3599010));
    }


}