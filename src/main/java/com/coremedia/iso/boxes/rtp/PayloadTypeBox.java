package com.coremedia.iso.boxes.rtp;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.AbstractBox;
import com.coremedia.iso.boxes.Box;

import java.io.IOException;

/**
 * The largest packet, in bytes; includes 12-byte RTP header.
 */
public class PayloadTypeBox extends AbstractBox {
    public static final String TYPE = "payt";

    long payloadNumber;
    String rtpMapString;

    public PayloadTypeBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    protected long getContentSize() {
        return utf8StringLengthInBytes(rtpMapString) + 5;
    }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        this.payloadNumber = in.readUInt32();
        int count = in.readUInt8();
        this.rtpMapString = in.readString(count);
    }

    public String getDisplayName() {
        return "Payload Type";
    }

    protected void getContent(IsoOutputStream os) throws IOException {
        os.writeUInt32(payloadNumber);
        os.writeUInt8(rtpMapString.length());
        os.writeStringNoTerm(rtpMapString);
    }

    public long getPayloadNumber() {
        return payloadNumber;
    }

    public void setPayloadNumber(long payloadNumber) {
        this.payloadNumber = payloadNumber;
    }

    public String getRtpMapString() {
        return rtpMapString;
    }

    public void setRtpMapString(String rtpMapString) {
        this.rtpMapString = rtpMapString;
    }
}
