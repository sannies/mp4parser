package com.coremedia.iso.boxes.apple;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * itunes MetaData comment box.
 */
public class AppleStoreAccountTypeBox extends AbstractAppleMetaDataBox {
    public static final String TYPE = "akID";


    public AppleStoreAccountTypeBox() {
        super(TYPE);
    }

    public String getReadableValue() {
        int value = 0;
        switch (value) {
            case 0:
                return "iTunes Account";
            case 1:
                return "AOL Account";
            default:
                return "unknown Account";
        }

    }
}