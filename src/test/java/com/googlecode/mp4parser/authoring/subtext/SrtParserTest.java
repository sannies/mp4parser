package com.googlecode.mp4parser.authoring.subtext;

import com.googlecode.mp4parser.authoring.Track;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;


public class SrtParserTest {
    @Test
    public void test() throws IOException {
        InputStream is = SrtParserTest.class.getResourceAsStream("/subs.srt");
        Track track = SrtParser.parse(is);
    }
}
