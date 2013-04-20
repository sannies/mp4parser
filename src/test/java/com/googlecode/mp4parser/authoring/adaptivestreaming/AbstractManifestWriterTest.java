package com.googlecode.mp4parser.authoring.adaptivestreaming;

import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;


public class AbstractManifestWriterTest {
    @Test
    public void testGetBitrate() throws Exception {
        FileChannel fc = new FileInputStream(
                this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile() +
                "Beethoven - Bagatelle op.119 no.11 i.m4a").getChannel();
        Movie m = MovieCreator.build(fc);
        Track t = m.getTracks().get(0);
        AbstractManifestWriter amw = new AbstractManifestWriter(null) {
            public String getManifest(Movie inputs) throws IOException {
                return null;
            }
        };
        Assert.assertEquals(127931, amw.getBitrate(t));
    }
}
