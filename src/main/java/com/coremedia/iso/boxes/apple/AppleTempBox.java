package com.coremedia.iso.boxes.apple;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * Beats per minute.
 */
public final class AppleTempBox extends AbstractAppleMetaDataBox {
    public static final String TYPE = "tmpo";


    public AppleTempBox() {
        super(TYPE);
    }


}