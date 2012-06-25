package com.coremedia.iso.boxes.mdat;


import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.boxes.MovieBox;
import com.coremedia.iso.boxes.TrackBox;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.builder.FragmentedMp4Builder;
import com.googlecode.mp4parser.authoring.builder.TwoSecondIntersectionFinder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.Iterator;

import static com.googlecode.mp4parser.util.CastUtils.l2i;

public class SampleListTest {
    ByteBuffer getMdatContent(FileChannel fc) throws IOException {

        while (fc.size() - fc.position() > 8) {
            long start = fc.position();
            ByteBuffer bb = ByteBuffer.allocate(8);
            fc.read(bb);
            bb.rewind();
            long size = IsoTypeReader.readUInt32(bb);
            String type = IsoTypeReader.read4cc(bb);
            long end = start + size;
            if (type.equals("mdat")) {
                ByteBuffer mdatContent = ByteBuffer.allocate(l2i(size));
                fc.read(mdatContent);
                mdatContent.rewind();
                return mdatContent;
            }


            fc.position(end);

        }
        Assert.fail("No mdat found!?!");
        return null;
    }

    @Test
    public void testGotAll() throws IOException {
        File originalFile = File.createTempFile("SampleListTest", "testGotAll");
        FileOutputStream fos = new FileOutputStream(originalFile);
        byte[] content = IOUtils.toByteArray(getClass().getResourceAsStream("/Beethoven - Bagatelle op.119 no.11 i.m4a"));
        fos.write(content);
        fos.close();

        FileChannel fc = new RandomAccessFile(originalFile, "r").getChannel();
        IsoFile isoFile = new IsoFile(fc);

        TrackBox tb = isoFile.getBoxes(MovieBox.class).get(0).getBoxes(TrackBox.class).get(0);
        SampleList sl = new SampleList(tb);
        ByteBuffer mdatContent = getMdatContent(new RandomAccessFile(originalFile, "r").getChannel());

        for (ByteBuffer sample : sl) {

            while (sample.remaining() > 0) {
                byte ist = sample.get();
                byte soll = mdatContent.get();
                Assert.assertEquals("Offset " + mdatContent.position() + " soll: " + soll + " ist: " + ist, soll, ist);
            }

        }
        fc.close();
        Assert.assertTrue(originalFile.delete());
    }

/*    @Test
    public void testGotAllWithMappingFailed() throws IOException {
        MediaDataBox.FAKE_MAPPING_FAIL = true;
        File originalFile = File.createTempFile("SampleListTest", "testGotAllWithMappingFailed");
        FileOutputStream fos = new FileOutputStream(originalFile);
        byte[] content = IOUtils.toByteArray(getClass().getResourceAsStream("/Beethoven - Bagatelle op.119 no.11 i.m4a"));
        fos.write(content);
        fos.close();
        IsoFile isoFile = new IsoFile(new RandomAccessFile(originalFile, "r").getChannel());

        TrackBox tb = isoFile.getBoxes(MovieBox.class).get(0).getBoxes(TrackBox.class).get(0);
        SampleList sl = new SampleList(tb);

        ByteBuffer mdatContent = getMdatContent(new RandomAccessFile(originalFile, "r").getChannel());
        for (ByteBuffer sample : sl) {

            while (sample.remaining() > 0) {
                byte ist = sample.get();
                byte soll = mdatContent.get();
                Assert.assertEquals("Offset " + mdatContent.position() + " soll: " + soll + " ist: " + ist, soll, ist);
            }

        }
        Assert.assertTrue(originalFile.delete());
    }                                          */

    @Test
    public void testFragmented() throws IOException {
        File fragFile = File.createTempFile("SampleListTest", "testFragmented");
        FileOutputStream fos = new FileOutputStream(fragFile);

        Movie m = MovieCreator.build(Channels.newChannel(getClass().getResourceAsStream("/Beethoven - Bagatelle op.119 no.11 i.m4a")));
        IsoFile orig = new IsoFile(Channels.newChannel(getClass().getResourceAsStream("/Beethoven - Bagatelle op.119 no.11 i.m4a")));
        SampleList slOrig = new SampleList(orig.getMovieBox().getBoxes(TrackBox.class).get(0));

        FragmentedMp4Builder fragmentedMp4Builder = new FragmentedMp4Builder();
        fragmentedMp4Builder.setIntersectionFinder(new TwoSecondIntersectionFinder());
        IsoFile isoFile = fragmentedMp4Builder.build(m);
        isoFile.getBox(fos.getChannel());
        fos.close();

        IsoFile fragmented = new IsoFile(new FileInputStream(fragFile).getChannel());
        SampleList slFrag = new SampleList(fragmented.getMovieBox().getBoxes(TrackBox.class).get(0));

        Assert.assertEquals(slOrig.size(), slFrag.size());

        Iterator<ByteBuffer> origBBIt = slOrig.iterator();
        Iterator<ByteBuffer> fragBBIt = slFrag.iterator();
        while (origBBIt.hasNext() && fragBBIt.hasNext()) {
            ByteBuffer origSample = origBBIt.next();
            ByteBuffer fragSample = fragBBIt.next();
            Assert.assertEquals(origSample, fragSample);
        }

    }
}
