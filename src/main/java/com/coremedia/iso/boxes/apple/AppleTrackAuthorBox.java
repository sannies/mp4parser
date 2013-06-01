package com.coremedia.iso.boxes.apple;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 */
public final class AppleTrackAuthorBox extends AbstractAppleMetaDataBox {
    public static final String TYPE = "\u00a9wrt";


    public AppleTrackAuthorBox() {
        super(TYPE);
    }


}