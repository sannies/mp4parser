package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.*;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.BoxContainer;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

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


    public void parse(IsoBufferWrapper in, long size, BoxFactory boxFactory, Box lastMovieFragmentBox) throws IOException {
        long sp = in.position();
        long dataBoxSize = in.readUInt32();
        String thisShouldBeData = in.readString(4);
        assert "data".equals(thisShouldBeData);
        appleDataBox = new AppleDataBox();
        appleDataBox.parse(in, dataBoxSize - 8, boxFactory, lastMovieFragmentBox);
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

    public void setValue(String value)  {
        if (appleDataBox.getFlags() == 1) {
            appleDataBox = new AppleDataBox();
            appleDataBox.setVersion(0);
            appleDataBox.setFlags(1);
            appleDataBox.setFourBytes(new byte[4]);
            appleDataBox.setContent(Utf8.convert(value));
        } else if (appleDataBox.getFlags() == 21) {
            appleDataBox = new AppleDataBox();
            appleDataBox.setVersion(0);
            appleDataBox.setFlags(21);
            appleDataBox.setFourBytes(new byte[4]);
            appleDataBox.setContent(new byte[]{(byte) (Byte.parseByte(value) & 0xFF)});
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
            return "" + appleDataBox.getContent()[0];
        }  else if (appleDataBox.getFlags() == 0) {
            return Hex.encodeHexString(appleDataBox.getContent());
        } else {
            return "unknown";
        }
    }
}
