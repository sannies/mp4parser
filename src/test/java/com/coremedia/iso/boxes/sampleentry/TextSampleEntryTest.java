package com.coremedia.iso.boxes.sampleentry;

import com.coremedia.iso.IsoFile;
import org.junit.Assert;
import org.junit.Test;

public class TextSampleEntryTest {

    @Test
    public void testBitSetters() {
        TextSampleEntry tx3g = new TextSampleEntry(IsoFile.fourCCtoBytes("tx3g"));
        tx3g.setContinuousKaraoke(true);
        Assert.assertTrue(tx3g.isContinuousKaraoke());
        tx3g.setContinuousKaraoke(false);
        Assert.assertFalse(tx3g.isContinuousKaraoke());
    }
}
