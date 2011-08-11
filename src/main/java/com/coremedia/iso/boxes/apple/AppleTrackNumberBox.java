package com.coremedia.iso.boxes.apple;

/**
 *
 */
public final class AppleTrackNumberBox extends AbstractAppleMetaDataBox {
    public static final String TYPE = "trkn";


    public AppleTrackNumberBox() {
        super(TYPE);
    }


    /**
     * @param track the actual track number
     * @param of    number of tracks overall
     */
    public void setTrackNumber(byte track, byte of) {
        appleDataBox = new AppleDataBox();
        appleDataBox.setVersion(0);
        appleDataBox.setFlags(0);
        appleDataBox.setFourBytes(new byte[4]);
        appleDataBox.setContent(new byte[]{0, 0, 0, track, 0, of, 0, 0});
    }

    public byte getTrackNumber() {
        return appleDataBox.getContent()[3];
    }

    public byte getNumberOfTracks() {
        return appleDataBox.getContent()[5];
    }

    public void setNumberOfTracks(byte numberOfTracks) {
        byte[] content = appleDataBox.getContent();
        content[5] = numberOfTracks;
        appleDataBox.setContent(content);
    }

    public void setTrackNumber(byte trackNumber) {
        byte[] content = appleDataBox.getContent();
        content[3] = trackNumber;
        appleDataBox.setContent(content);
    }


}