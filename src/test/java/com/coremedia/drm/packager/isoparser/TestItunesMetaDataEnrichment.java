package com.coremedia.drm.packager.isoparser;

import com.coremedia.iso.ByteArrayRandomAccessDataSource;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.HandlerBox;
import com.coremedia.iso.boxes.MetaBox;
import com.coremedia.iso.boxes.UserDataBox;
import com.coremedia.iso.boxes.apple.AppleItemListBox;
import com.coremedia.iso.boxes.apple.AppleRecordingYearBox;
import com.coremedia.iso.boxes.odf.OmaDrmContainerBox;
import com.coremedia.iso.boxes.odf.OmaDrmDiscreteHeadersBox;
import junit.framework.TestCase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 */
public class TestItunesMetaDataEnrichment extends TestCase {
  public void testEnrichment() throws IOException {
    IsoFile isoFile = new IsoFile(new ByteArrayRandomAccessDataSource(getClass().getResourceAsStream("/file6141.odf")));
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
