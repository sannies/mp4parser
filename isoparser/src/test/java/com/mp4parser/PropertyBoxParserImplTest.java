package com.mp4parser;

import com.mp4parser.PropertyBoxParserImpl;
import org.junit.Assert;
import org.junit.Test;

public class PropertyBoxParserImplTest {


    @Test
    public void test_isoparser_custom_properties() {
        PropertyBoxParserImpl bp = new PropertyBoxParserImpl();
        Assert.assertEquals("b", bp.mapping.get("a"));
    }

}
