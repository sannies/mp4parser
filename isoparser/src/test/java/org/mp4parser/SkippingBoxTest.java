package org.mp4parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mp4parser.boxes.iso14496.part12.MovieBox;
import org.mp4parser.boxes.iso14496.part12.TrackHeaderBox;
import org.mp4parser.tools.Path;
import org.mp4parser.tools.PathTest;

public class SkippingBoxTest {
    
    private IsoFile isoFile;
    
    @Before
    public void setup() throws IOException {
        FileInputStream fis = new FileInputStream(PathTest.class.getProtectionDomain().getCodeSource().getLocation().getFile() + "/test.m4p");
        isoFile = new IsoFile(fis.getChannel(), new PropertyBoxParserImpl().skippingBoxes("mdat", "mvhd"));
        fis.close();
    }


    @Test
    public void testBoxesHaveBeenSkipped() {
        MovieBox movieBox = isoFile.getMovieBox();
        assertNotNull(movieBox);
        assertEquals(4, movieBox.getBoxes().size());
        assertEquals("mvhd", movieBox.getBoxes().get(0).getType());
        assertEquals("iods", movieBox.getBoxes().get(1).getType());
        assertEquals("trak", movieBox.getBoxes().get(2).getType());
        assertEquals("udta", movieBox.getBoxes().get(3).getType());
        
        Box box = Path.getPath(isoFile, "moov/trak/tkhd");
        assertTrue( box instanceof TrackHeaderBox );
        
        TrackHeaderBox thb = (TrackHeaderBox)box;
        assertTrue(thb.getDuration() == 102595);
        
        box = Path.getPath(isoFile, "mdat");
        assertTrue(box instanceof SkipBox);
        
        box = Path.getPath(isoFile, "moov/mvhd");
        assertTrue(box instanceof SkipBox);
    }

}
