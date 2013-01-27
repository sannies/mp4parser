package com.coremedia.iso.boxes.apple;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * itunes MetaData comment box.
 */
public final class AppleCopyrightBox extends AbstractAppleMetaDataBox {
    public static final String TYPE = "cprt";


    public AppleCopyrightBox() {
        super(TYPE);
        appleDataBox = AppleDataBox.getStringAppleDataBox();
    }

}