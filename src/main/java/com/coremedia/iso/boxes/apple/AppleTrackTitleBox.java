package com.coremedia.iso.boxes.apple;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 */
public final class AppleTrackTitleBox extends AbstractAppleMetaDataBox {
    public static final String TYPE = "\u00a9nam";


    public AppleTrackTitleBox() {
        super(TYPE);
    }

}
