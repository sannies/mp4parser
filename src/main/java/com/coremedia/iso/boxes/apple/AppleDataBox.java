package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.AbstractFullBox;
import com.coremedia.iso.boxes.Box;

import java.io.IOException;

/**
 * Most stupid box of the world. Encapsulates actual data within
 */
public final class AppleDataBox extends AbstractFullBox {
    public static final String TYPE = "data";

    private byte[] fourBytes = new byte[4];
    private byte[] content;

    private static AppleDataBox getEmpty() {
        AppleDataBox appleDataBox = new AppleDataBox();
        appleDataBox.setVersion(0);
        appleDataBox.setFourBytes(new byte[4]);
        return appleDataBox;
    }

    public static AppleDataBox getStringAppleDataBox() {
        AppleDataBox appleDataBox = getEmpty();
        appleDataBox.setFlags(1);
        appleDataBox.setContent(new byte[]{0});
        return appleDataBox;
    }

    public static AppleDataBox getUint8AppleDataBox() {
        AppleDataBox appleDataBox = new AppleDataBox();
        appleDataBox.setFlags(21);
        appleDataBox.setContent(new byte[]{0});
        return appleDataBox;
    }

    public static AppleDataBox getUint16AppleDataBox() {
        AppleDataBox appleDataBox = new AppleDataBox();
        appleDataBox.setFlags(21);
        appleDataBox.setContent(new byte[]{0, 0});
        return appleDataBox;
    }

    public static AppleDataBox getUint32AppleDataBox() {
        AppleDataBox appleDataBox = new AppleDataBox();
        appleDataBox.setFlags(21);
        appleDataBox.setContent(new byte[]{0, 0, 0, 0});
        return appleDataBox;
    }

    public AppleDataBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    protected long getContentSize() {
        return content.length + 4;
    }

    public void setContent(byte[] content) {
        this.content = new byte[content.length];
        System.arraycopy(content, 0, this.content, 0, content.length);
    }

    public void setFourBytes(byte[] fourBytes) {
        System.arraycopy(fourBytes, 0, this.fourBytes, 0, 4);
    }

    @Override
    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        fourBytes[0] = (byte) in.read();
        fourBytes[1] = (byte) in.read();
        fourBytes[2] = (byte) in.read();
        fourBytes[3] = (byte) in.read();
        size -= (4 + 4);
        content = in.read((int) size);

    }

    protected void getContent(IsoOutputStream os) throws IOException {
        os.write(fourBytes, 0, 4);
        os.write(content);
    }

    public byte[] getFourBytes() {
        return fourBytes;
    }

    public byte[] getContent() {
        return content;
    }
}
