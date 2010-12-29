package com.coremedia.iso.boxes.apple;

import java.util.logging.Logger;

/**
 *
 */
public final class AppleCoverBox extends AbstractAppleMetaDataBox {
    private static Logger LOG = Logger.getLogger(AppleCoverBox.class.getName());
    public static final String TYPE = "covr";


    public AppleCoverBox() {
        super(TYPE);
    }

    public String getDisplayName() {
        return "iTunes Cover";
    }


    public void setPng(byte[] pngData) {
        appleDataBox = new AppleDataBox();
        appleDataBox.setVersion(0);
        appleDataBox.setFlags(0xe);
        appleDataBox.setFourBytes(new byte[4]);
        appleDataBox.setContent(pngData);
    }


    public void setJpg(byte[] jpgData) {
        appleDataBox = new AppleDataBox();
        appleDataBox.setVersion(0);
        appleDataBox.setFlags(0xd);
        appleDataBox.setFourBytes(new byte[4]);
        appleDataBox.setContent(jpgData);
    }

    @Override
    public void setValue(String value) {
        LOG.warning("Ignored setter");
    }

    @Override
    public String getValue() {
        return "Value is ignored";
    }
}