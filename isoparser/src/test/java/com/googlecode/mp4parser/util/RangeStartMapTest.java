package com.googlecode.mp4parser.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mp4parser.tools.RangeStartMap;

public class RangeStartMapTest {

    @Test
    public void basicTest() {
        RangeStartMap<Integer, String> a = new RangeStartMap<Integer, String>();
        a.put(0, "Null");
        a.put(10, "Zehn");
        a.put(20, null);
        a.put(30, "Dreißig");

        Assertions.assertEquals("Null", a.get(0));
        Assertions.assertEquals("Null", a.get(1));
        Assertions.assertEquals("Null", a.get(9));
        Assertions.assertEquals("Zehn", a.get(10));
        Assertions.assertEquals("Zehn", a.get(11));
        Assertions.assertEquals("Zehn", a.get(19));
        Assertions.assertNull(a.get(20));
        Assertions.assertNull(a.get(21));
        Assertions.assertNull(a.get(29));
        Assertions.assertEquals("Dreißig", a.get(30));
        Assertions.assertEquals("Dreißig", a.get(31));
        Assertions.assertEquals("Dreißig", a.get(Integer.MAX_VALUE));
    }

}