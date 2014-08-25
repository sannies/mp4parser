package com.googlecode.mp4parser;

import com.coremedia.iso.boxes.FileTypeBox;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.security.SecureRandom;
import java.util.Arrays;

import static org.junit.Assert.*;

public class BasicContainerTest {

    @Test
    public void testGetByteBuffer() throws IOException {
        BasicContainer bc = new BasicContainer();
        FileTypeBox ftyp1 = new FileTypeBox("1234", 213, Arrays.asList("cenc", "denc", "benc"));
        FileTypeBox ftyp2 = new FileTypeBox("1234", 2213, Arrays.asList("cenc", "denc", "benc"));
        FileTypeBox ftyp3 = new FileTypeBox("1234", 22213, Arrays.asList("cenc", "denc", "benc"));
        bc.addBox(ftyp1);
        bc.addBox(ftyp2);
        bc.addBox(ftyp3);
        ByteArrayOutputStream orig = new ByteArrayOutputStream();
        WritableByteChannel wc = Channels.newChannel(orig);
        bc.writeContainer(wc);

        for (int i = 0; i < orig.size(); i++) {
            ByteBuffer bb1 = bc.getByteBuffer(0, i);
            ByteBuffer bb2 = bc.getByteBuffer(i, orig.size() - i);
            ByteArrayOutputStream check = new ByteArrayOutputStream();
            WritableByteChannel wcCheck = Channels.newChannel(check);
            wcCheck.write(bb1);
            wcCheck.write(bb2);

            Assert.assertArrayEquals("Test " + i, orig.toByteArray(), check.toByteArray());
        }

        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 100; i++) {
            int firstLen = random.nextInt(orig.size()-1);
            int secondLen = random.nextInt(orig.size() - firstLen );


            ByteBuffer bb1 = bc.getByteBuffer(0, firstLen);
            ByteBuffer bb2 = bc.getByteBuffer(firstLen, secondLen);
            ByteBuffer bb3 = bc.getByteBuffer(firstLen + secondLen, orig.size() - (firstLen + secondLen));

            ByteArrayOutputStream check = new ByteArrayOutputStream();
            WritableByteChannel wcCheck = Channels.newChannel(check);
            wcCheck.write(bb1);
            wcCheck.write(bb2);
            wcCheck.write(bb3);

            Assert.assertArrayEquals("Test " + firstLen + "|" + secondLen , orig.toByteArray(), check.toByteArray());
        }

    }

}