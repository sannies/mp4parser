package org.mp4parser;

import org.junit.Assert;
import org.junit.Test;

public class PropertyBoxParserImplTest {


    @Test
    public void test_isoparser_custom_properties() {
        PropertyBoxParserImpl.BOX_MAP_CACHE = null;
        PropertyBoxParserImpl bp = new PropertyBoxParserImpl();
        Assert.assertEquals("b", bp.mapping.get("a"));
    }

}
