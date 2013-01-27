package com.coremedia.iso.boxes.apple;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * iTunes Rating Box.
 */
public final class AppleRatingBox extends AbstractAppleMetaDataBox {
    public static final String TYPE = "rtng";


    public AppleRatingBox() {
        super(TYPE);
        appleDataBox = AppleDataBox.getUint8AppleDataBox();
    }


}
