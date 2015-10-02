package com.googlecode.mp4parser.authoring.tracks.webvtt;

import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.*;

/**
 * Created by sannies on 27.08.2015.
 */
public class WebVttTrackTest {
    @Test
    public void testVerySimpleCheck() throws Exception {
        WebVttTrack t1 = new WebVttTrack(WebVttTrackTest.class.getResourceAsStream("Tears_Of_Steel_per.vtt"), "test", Locale.forLanguageTag("per"));
        WebVttTrack t2 = new WebVttTrack(WebVttTrackTest.class.getResourceAsStream("Tears_Of_Steel_rus.vtt"), "test", Locale.forLanguageTag("rus"));
        WebVttTrack t3 = new WebVttTrack(WebVttTrackTest.class.getResourceAsStream("Tears_Of_Steel_nld.vtt"), "test", Locale.forLanguageTag("nld"));



        assertEquals(".تو یه احمقی،تام", new String(t1.getSamples().get(1).asByteBuffer().array(), 16, t1.getSamples().get(1).asByteBuffer().array().length - 16, "UTF-8"));
        assertEquals("Ты придурок, Том!", new String(t2.getSamples().get(1).asByteBuffer().array(), 16, t2.getSamples().get(1).asByteBuffer().array().length - 16, "UTF-8"));
        assertEquals("Je bent een eikel, Thom.", new String(t3.getSamples().get(1).asByteBuffer().array(), 16, t3.getSamples().get(1).asByteBuffer().array().length - 16, "UTF-8"));

    }
}