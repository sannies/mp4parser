package com.coremedia.iso.boxes.apple;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * itunes MetaData comment box.
 */
public final class AppleCommentBox extends AbstractAppleMetaDataBox {
    public static final String TYPE = "\u00a9cmt";


    public AppleCommentBox() {
        super(TYPE);
    }


}
