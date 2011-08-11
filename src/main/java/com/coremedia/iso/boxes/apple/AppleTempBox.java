package com.coremedia.iso.boxes.apple;

/**
 * Beats per minute.
 */
public final class AppleTempBox extends AbstractAppleMetaDataBox {
    public static final String TYPE = "tmpo";


    public AppleTempBox() {
        super(TYPE);
        appleDataBox = AppleDataBox.getUint16AppleDataBox();
    }


    public int getTempo() {
        return appleDataBox.getContent()[1];
    }

    public void setTempo(int tempo) {
        appleDataBox = new AppleDataBox();
        appleDataBox.setVersion(0);
        appleDataBox.setFlags(21);
        appleDataBox.setFourBytes(new byte[4]);
        appleDataBox.setContent(new byte[]{0, (byte) (tempo & 0xFF)});

    }
}