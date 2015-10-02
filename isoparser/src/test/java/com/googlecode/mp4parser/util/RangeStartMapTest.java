package com.googlecode.mp4parser.util;

import junit.framework.Assert;
import org.junit.Test;
import org.mp4parser.tools.RangeStartMap;

public class RangeStartMapTest {

    @Test
    public void basicTest() {
        RangeStartMap<Integer, String> a = new RangeStartMap<Integer, String>();
        a.put(0, "Null");
        a.put(10, "Zehn");
        a.put(20, null);
        a.put(30, "Dreißig");

        Assert.assertEquals("Null", a.get(0));
        Assert.assertEquals("Null", a.get(1));
        Assert.assertEquals("Null", a.get(9));
        Assert.assertEquals("Zehn", a.get(10));
        Assert.assertEquals("Zehn", a.get(11));
        Assert.assertEquals("Zehn", a.get(19));
        Assert.assertEquals(null, a.get(20));
        Assert.assertEquals(null, a.get(21));
        Assert.assertEquals(null, a.get(29));
        Assert.assertEquals("Dreißig", a.get(30));
        Assert.assertEquals("Dreißig", a.get(31));
        Assert.assertEquals("Dreißig", a.get(Integer.MAX_VALUE));
    }

}