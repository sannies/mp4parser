package com.googlecode.mp4parser.authoring.subtext;

import com.googlecode.mp4parser.authoring.tracks.TextTrackImpl;
import com.googlecode.mp4parser.srt.SrtParser;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;


public class SrtParserTest {
    @Test
    public void test() throws IOException {
        InputStream is = SrtParserTest.class.getResourceAsStream("/subs.srt");
        TextTrackImpl track = SrtParser.parse(is);
    }
}
