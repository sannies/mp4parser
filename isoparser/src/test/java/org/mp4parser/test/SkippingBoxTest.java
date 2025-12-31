package org.mp4parser.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mp4parser.Box;
import org.mp4parser.IsoFile;
import org.mp4parser.PropertyBoxParserImpl;
import org.mp4parser.SkipBox;
import org.mp4parser.boxes.iso14496.part12.MovieBox;
import org.mp4parser.boxes.iso14496.part12.TrackHeaderBox;
import org.mp4parser.tools.Path;
import org.mp4parser.test.tools.PathTest;

public class SkippingBoxTest {
    
    private IsoFile isoFile;
    
    @BeforeEach
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
