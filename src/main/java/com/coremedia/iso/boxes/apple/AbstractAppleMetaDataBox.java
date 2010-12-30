package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.Utf8;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.BoxContainer;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.logging.Logger;

/**
 *
 */
public abstract class AbstractAppleMetaDataBox extends Box implements BoxContainer {
    private static Logger LOG = Logger.getLogger(AbstractAppleMetaDataBox.class.getName());
    AppleDataBox appleDataBox = new AppleDataBox();

    public Box[] getBoxes() {
        return new Box[]{appleDataBox};
    }

    public <T extends Box> T[] getBoxes(Class<T> clazz) {
        if (appleDataBox.getClass().isInstance(clazz)) {
            T[] returnValue = (T[]) Array.newInstance(clazz, 1);
            returnValue[0] = (T) appleDataBox;
            return returnValue;
        }
        return null;
    }

    public AbstractAppleMetaDataBox(String type) {
        super(IsoFile.fourCCtoBytes(type));
    }


    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        long sp = in.position();
        long dataBoxSize = in.readUInt32();
        String thisShouldBeData = in.readString(4);
        assert "data".equals(thisShouldBeData);
        appleDataBox = new AppleDataBox();
        appleDataBox.parse(in, dataBoxSize - 8, boxParser, lastMovieFragmentBox);
        appleDataBox.setParent(this);
        appleDataBox.offset = sp;
    }


    protected long getContentSize() {
        return appleDataBox.getSize();
    }

    protected void getContent(IsoOutputStream os) throws IOException {
        appleDataBox.getBox(os);
    }

    public long getNumOfBytesToFirstChild() {
        return getSize() - appleDataBox.getSize();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" +
                "appleDataBox=" + appleDataBox +
                '}';
    }

    static long toLong(byte b) {
        return b < 0 ? b + 256 : b;
    }

    public void setValue(String value) {
        if (appleDataBox.getFlags() == 1) {
            appleDataBox = new AppleDataBox();
            appleDataBox.setVersion(0);
            appleDataBox.setFlags(1);
            appleDataBox.setFourBytes(new byte[4]);
            appleDataBox.setContent(Utf8.convert(value));
        } else if (appleDataBox.getFlags() == 21) {
            byte[] content = appleDataBox.getContent();
            appleDataBox = new AppleDataBox();
            appleDataBox.setVersion(0);
            appleDataBox.setFlags(21);
            appleDataBox.setFourBytes(new byte[4]);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IsoOutputStream isoOutputStream = new IsoOutputStream(baos);
            try {
                if (content.length == 1) {
                    isoOutputStream.writeUInt8((Byte.parseByte(value) & 0xFF));
                } else if (content.length == 2) {
                    isoOutputStream.writeUInt16(Integer.parseInt(value));
                } else if (content.length == 4) {
                    isoOutputStream.writeUInt32(Long.parseLong(value));
                } else if (content.length == 8) {
                    isoOutputStream.writeUInt64(Long.parseLong(value));
                } else {
                    throw new Error("The content length within the appleDataBox is neither 1, 2, 4 or 8. I can't handle that!");
                }
            } catch (IOException e) {
                throw new Error(e);
            }
            appleDataBox.setContent(content);
        } else if (appleDataBox.getFlags() == 0) {
            appleDataBox = new AppleDataBox();
            appleDataBox.setVersion(0);
            appleDataBox.setFlags(0);
            appleDataBox.setFourBytes(new byte[4]);
            try {
                appleDataBox.setContent(Hex.decodeHex(value.toCharArray()));
            } catch (DecoderException e) {
                throw new IllegalArgumentException("The value has to be a hex string", e);
            }

        } else {
            LOG.warning("Don't know how to handle appleDataBox with flag=" + appleDataBox.getFlags());
        }
    }

    public String getValue() {
        if (appleDataBox.getFlags() == 1) {
            return Utf8.convert(appleDataBox.getContent());
        } else if (appleDataBox.getFlags() == 21) {
            byte[] content = appleDataBox.getContent();
            long l = 0;
            int current = 1;
            int length = content.length;
            for (byte b : content) {
                l += toLong(b)<< (8*(length - current++));
            }
            return "" + l;
        } else if (appleDataBox.getFlags() == 0) {
            return Hex.encodeHexString(appleDataBox.getContent());
        } else {
            return "unknown";
        }
    }
}
