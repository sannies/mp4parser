package com.coremedia.drm.packager.isoparser;

import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
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

/**
 *
 */
public class TestItunesMetaDataEnrichment extends TestCase {
  public void testEnrichment() throws IOException {
    InputStream is = getClass().getResourceAsStream("/file6141.odf");
    IsoBufferWrapper isoBufferWrapper = new IsoBufferWrapper(ByteBuffer.wrap(IOUtils.toByteArray(is)));
    IsoFile isoFile = new IsoFile(isoBufferWrapper);
    isoFile.parse();
    OmaDrmContainerBox omaDrmContainerBox = isoFile.getBoxes(OmaDrmContainerBox.class)[0];
    OmaDrmDiscreteHeadersBox omaDrmDiscreteHeadersBox = omaDrmContainerBox.getBoxes(OmaDrmDiscreteHeadersBox.class)[0];
    UserDataBox[] userDataBoxes = omaDrmDiscreteHeadersBox.getBoxes(UserDataBox.class);
    MetaBox mb = new MetaBox();

    HandlerBox hb = new HandlerBox();
    hb.setHandlerType("mdir");
    hb.setName("");
    mb.addBox(hb);

    AppleItemListBox ilst = new AppleItemListBox();
    AppleRecordingYearBox appleRecordingYearBox = new AppleRecordingYearBox();
    appleRecordingYearBox.setRecordingYear("2008");
    ilst.addBox(appleRecordingYearBox);
    mb.addBox(ilst);

    System.err.print(mb);
    userDataBoxes[0].addBox(mb);

    File f = File.createTempFile("TestItunesMetaDataEnrichment", "odf");
    f.deleteOnExit();
    FileOutputStream fos = new FileOutputStream(f);
    isoFile.write(fos);
    fos.close();
    System.err.println(f);
    System.err.println(f);
  }
}
