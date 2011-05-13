package com.coremedia.drm.packager.isoparser;

import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.HandlerBox;
import com.coremedia.iso.boxes.MetaBox;
import com.coremedia.iso.boxes.UserDataBox;
import com.coremedia.iso.boxes.apple.AppleItemListBox;
import com.coremedia.iso.boxes.apple.AppleRecordingYearBox;
import com.coremedia.iso.boxes.odf.OmaDrmContainerBox;
import com.coremedia.iso.boxes.odf.OmaDrmDiscreteHeadersBox;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

/**
 *
 */
public class TestItunesMetaDataEnrichment extends TestCase {
  public void testEnrichment() throws IOException {
        InputStream is = getClass().getResourceAsStream("/file6141.odf");
        IsoBufferWrapper isoBufferWrapper =
                new IsoBufferWrapper(ByteBuffer.wrap(IOUtils.toByteArray(is)));
        IsoFile isoFile = new IsoFile(isoBufferWrapper);
        isoFile.parse();
        OmaDrmContainerBox omaDrmContainerBox =
                isoFile.getBoxes(OmaDrmContainerBox.class, false).get(0);
        OmaDrmDiscreteHeadersBox omaDrmDiscreteHeadersBox =
                omaDrmContainerBox.getBoxes(OmaDrmDiscreteHeadersBox.class).get(0);
        List<UserDataBox> userDataBoxes =
                omaDrmDiscreteHeadersBox.getBoxes(UserDataBox.class, false);
        MetaBox mb = new MetaBox();

        HandlerBox hb = new HandlerBox();
        hb.setHandlerType("mdir");
        hb.setName("");
        mb.addBox(hb);

        AppleItemListBox ilst = new AppleItemListBox();
        AppleRecordingYearBox appleRecordingYearBox = new AppleRecordingYearBox();
        appleRecordingYearBox.setValue("2008");
        ilst.addBox(appleRecordingYearBox);
        mb.addBox(ilst);

        System.err.print(mb);
        userDataBoxes.get(0).addBox(mb);

        File f = File.createTempFile("TestItunesMetaDataEnrichment", "odf");
        f.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(f);
        isoFile.getBox(new IsoOutputStream(fos));
        fos.close();
        System.err.println(f);
        System.err.println(f);
    }
}
