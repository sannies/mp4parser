package com.googlecode.mp4parser.authoring.tracks;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by sannies on 06.08.2015.
 */
public class SMPTETTTrackImplTest {
    @Test
    public void testToTime() throws Exception {
        Assert.assertEquals(-3599000, SMPTETTTrackImpl.toTime("-00:59:59:00"));
        Assert.assertEquals(3599000, SMPTETTTrackImpl.toTime("00:59:59:00"));
    }

    @Test
    public void testToTimeExpression() throws Exception {
        Assert.assertEquals("-00:59:59.009", SMPTETTTrackImpl.toTimeExpression(-3599009));
        Assert.assertEquals("00:59:59.010", SMPTETTTrackImpl.toTimeExpression(3599010));
    }


}