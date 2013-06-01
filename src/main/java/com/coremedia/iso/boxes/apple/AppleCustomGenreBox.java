package com.coremedia.iso.boxes.apple;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 */
public final class AppleCustomGenreBox extends AbstractAppleMetaDataBox {
    public static final String TYPE = "\u00a9gen";


    public AppleCustomGenreBox() {
        super(TYPE);
    }


}