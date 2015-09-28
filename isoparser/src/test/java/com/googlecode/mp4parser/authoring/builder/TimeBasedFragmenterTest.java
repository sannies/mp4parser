package com.googlecode.mp4parser.authoring.builder;

import com.coremedia.iso.Hex;
import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class TimeBasedFragmenterTest {

    /**
     * This test indicated that you changed the output. Do you expect that?
     */
    @Test
    public void stabilize() throws IOException, NoSuchAlgorithmException {
        Movie movie = MovieCreator.build(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile() +
                "/Beethoven - Bagatelle op.119 no.11 i.m4a");
        FragmentedMp4Builder mp4Builder = new FragmentedMp4Builder() {
            @Override
            public Date getDate() {
                return new Date(0);
            }
        };
        mp4Builder.setFragmenter(new TimeBasedFragmenter(2));
        Container fMp4 = mp4Builder.build(movie);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        fMp4.writeContainer(Channels.newChannel(baos));
        MessageDigest md = MessageDigest.getInstance("MD5");
        String digest = Hex.encodeHex(md.digest(baos.toByteArray()));
        System.err.println(digest);
        String oldDigest = "A6E5D718B6123A2768F56E3E44E33BE4";
        //new FileOutputStream("c:\\dev\\check.mp4").write(baos.toByteArray());
        Assert.assertEquals(oldDigest, digest);

    }

}
