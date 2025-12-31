package org.mp4parser.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mp4parser.PropertyBoxParserImpl;

public class PropertyBoxParserImplTest {


    @Test
    public void test_isoparser_custom_properties() {
        PropertyBoxParserImpl.BOX_MAP_CACHE = null;
        PropertyBoxParserImpl bp = new PropertyBoxParserImpl();
        Assertions.assertEquals("b", bp.mapping.get("a"));
    }

}
