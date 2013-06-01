package com.coremedia.iso.boxes.apple;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 */
public class AppleRecordingYearBox extends AbstractAppleMetaDataBox {
    public static final String TYPE = "\u00a9day";


    public AppleRecordingYearBox() {
        super(TYPE);
    }


}