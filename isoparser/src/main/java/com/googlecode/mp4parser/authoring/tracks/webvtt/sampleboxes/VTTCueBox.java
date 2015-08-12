package com.googlecode.mp4parser.authoring.tracks.webvtt.sampleboxes;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoTypeWriter;
import com.mp4parser.streaming.WriteOnlyBox;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

public class VTTCueBox extends WriteOnlyBox {
    CueSourceIDBox cueSourceIDBox; // optional source ID
    CueIDBox cueIDBox; // optional
    CueTimeBox cueTimeBox; // optional current time indication
    CueSettingsBox cueSettingsBox; // optional, cue settings
    CuePayloadBox cuePayloadBox; // the (mandatory) cue payload lines

    public VTTCueBox() {
        super("vtcc");
    }

    public long getSize() {
        return 8 +
                (cueSourceIDBox != null ? cueSourceIDBox.getSize() : 0) +
                (cueIDBox != null ? cueIDBox.getSize() : 0) +
                (cueTimeBox != null ? cueTimeBox.getSize() : 0) +
                (cueSettingsBox != null ? cueSettingsBox.getSize() : 0) +
                (cuePayloadBox != null ? cuePayloadBox.getSize() : 0);

    }

    public void getBox(WritableByteChannel writableByteChannel) throws IOException {
        ByteBuffer header = ByteBuffer.allocate(8);
        IsoTypeWriter.writeUInt32(header, getSize());
        header.put(IsoFile.fourCCtoBytes(getType()));
        writableByteChannel.write((ByteBuffer) header.rewind());
        if (cueSourceIDBox != null) {
            cueSourceIDBox.getBox(writableByteChannel);
        }
        if (cueIDBox != null) {
            cueIDBox.getBox(writableByteChannel);
        }
        if (cueTimeBox != null) {
            cueTimeBox.getBox(writableByteChannel);
        }
        if (cueSettingsBox != null) {
            cueSettingsBox.getBox(writableByteChannel);
        }
        if (cuePayloadBox != null) {
            cuePayloadBox.getBox(writableByteChannel);
        }
    }

    public CueSourceIDBox getCueSourceIDBox() {
        return cueSourceIDBox;
    }

    public void setCueSourceIDBox(CueSourceIDBox cueSourceIDBox) {
        this.cueSourceIDBox = cueSourceIDBox;
    }

    public CueIDBox getCueIDBox() {
        return cueIDBox;
    }

    public void setCueIDBox(CueIDBox cueIDBox) {
        this.cueIDBox = cueIDBox;
    }

    public CueTimeBox getCueTimeBox() {
        return cueTimeBox;
    }

    public void setCueTimeBox(CueTimeBox cueTimeBox) {
        this.cueTimeBox = cueTimeBox;
    }

    public CueSettingsBox getCueSettingsBox() {
        return cueSettingsBox;
    }

    public void setCueSettingsBox(CueSettingsBox cueSettingsBox) {
        this.cueSettingsBox = cueSettingsBox;
    }

    public CuePayloadBox getCuePayloadBox() {
        return cuePayloadBox;
    }

    public void setCuePayloadBox(CuePayloadBox cuePayloadBox) {
        this.cuePayloadBox = cuePayloadBox;
    }
}
