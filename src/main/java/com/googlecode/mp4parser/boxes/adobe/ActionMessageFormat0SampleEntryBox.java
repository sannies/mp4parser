package com.googlecode.mp4parser.boxes.adobe;

import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.sampleentry.SampleEntry;

import java.io.IOException;

/**
 * Sample Entry as used for Action Message Format tracks.
 */
public class ActionMessageFormat0SampleEntryBox extends SampleEntry {
    public ActionMessageFormat0SampleEntryBox() {
        super("amf0");
    }

    @Override
    protected long getContentSize() {
        long size = 8;
        for (Box box : boxes) {
            size += box.getSize();
        }

        return size;
    }

    @Override
    protected void getContent(IsoOutputStream os) throws IOException {
        os.write(new byte[6]);
        os.writeUInt16(getDataReferenceIndex());
        for (Box box : boxes) {
            box.getBox(os);
        }
    }
}
