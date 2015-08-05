package com.mp4parser.authoring.builder;

import com.mp4parser.RandomAccessSource;
import com.mp4parser.tools.Hex;
import com.mp4parser.authoring.Movie;
import com.mp4parser.authoring.container.mp4.MovieCreator;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * Created by sannies on 1/9/14.
 */
public class FragmentedMp4BuilderTest {

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
        mp4Builder.setIntersectionFinder(new TwoSecondIntersectionFinder(movie, 2));
        RandomAccessSource.Container fMp4 = mp4Builder.build(movie);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        fMp4.writeContainer(Channels.newChannel(baos));
        MessageDigest md = MessageDigest.getInstance("MD5");
        String digest = Hex.encodeHex(md.digest(baos.toByteArray()));
        System.err.println(digest);
        String oldDigest = "1E7B44CAA015E844D4DF1FB7C949D3C9";
        //new FileOutputStream("c:\\dev\\check.mp4").write(baos.toByteArray());
        Assert.assertEquals(oldDigest, digest);

    }

}
