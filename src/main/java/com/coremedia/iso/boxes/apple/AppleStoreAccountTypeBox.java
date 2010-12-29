package com.coremedia.iso.boxes.apple;

/**
 * itunes MetaData comment box.
 */
public class AppleStoreAccountTypeBox extends AbstractAppleMetaDataBox {
    public static final String TYPE = "akID";


    public AppleStoreAccountTypeBox() {
        super(TYPE);
    }

    public String getDisplayName() {
        return "iTunes Store Account Type Box";
    }

    public String getReadableValue() {
        byte value = this.appleDataBox.getContent()[0];
        switch (value) {
            case 0: return "iTunes Account";
            case 1: return "AOL Account";
            default: return "unknown Account";
        }

    }
}