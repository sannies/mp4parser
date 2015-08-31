package com.mp4parser.muxer;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;

public class MultiFileDataSourceImplTest {
    File a;
    File b;
    File c;

    @Before
    public void setUp() throws Exception {
        a = File.createTempFile("MultiFileDataSourceImplTest", "aaa");
        write(a, "aaaaaaaaaa");
        b = File.createTempFile("MultiFileDataSourceImplTest", "bbb");
        write(b, "bbbbbbbbbb");
        c = File.createTempFile("MultiFileDataSourceImplTest", "ccc");
        write(c, "cccccccccc");
    }

    @Test
    public void testWithIn() throws Exception {
        DataSource ds = new MultiFileDataSourceImpl(a, b, c);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Assert.assertEquals("a", check(ds, 0, 1));
        Assert.assertEquals("aa", check(ds, 0, 2));
        Assert.assertEquals("a", check(ds, 1, 1));
        Assert.assertEquals("aa", check(ds, 1, 2));
        Assert.assertEquals("aaaaaaaaaa", check(ds, 0, 10));
        Assert.assertEquals("aaaaaaaaaab", check(ds, 0, 11));
        Assert.assertEquals("aaaaaaab", check(ds, 3, 8));
        Assert.assertEquals("aaaaabbbbbbbbbbccccc", check(ds, 5, 20));
    }

    public String check(DataSource ds, int a, int b) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ds.transferTo(a, b, Channels.newChannel(baos));
        String result =  new String(baos.toByteArray());
        System.err.println(result);
        return result;
    }

    @After
    public void tearDown() throws Exception {
        a.deleteOnExit();
        b.deleteOnExit();
        c.deleteOnExit();
    }

    private void write(File f, String text) throws IOException {
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(text.getBytes());
        fos.close();
    }
}