package com.coremedia.iso.boxes.apple;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 */
public final class AppleSynopsisBox extends AbstractAppleMetaDataBox {
    public static final String TYPE = "ldes";


    public AppleSynopsisBox() {
        super(TYPE);
        appleDataBox = AppleDataBox.getStringAppleDataBox();
    }


}