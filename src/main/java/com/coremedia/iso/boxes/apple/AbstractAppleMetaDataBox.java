package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.Hex;
import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.googlecode.mp4parser.AbstractBox;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import static com.googlecode.mp4parser.util.CastUtils.l2i;

/**
 *
 */
public abstract class AbstractAppleMetaDataBox extends AbstractBox {
    private static Logger LOG = Logger.getLogger(AbstractAppleMetaDataBox.class.getName());

    byte[] data;

    public AbstractAppleMetaDataBox(String type) {
        super(type);
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        int dataBoxSize = l2i(IsoTypeReader.readUInt32(content));
        String dataString = IsoTypeReader.read4cc(content);
        assert "data".equals(dataString);
        data = new byte[dataBoxSize - 8];
    }


    protected long getContentSize() {
        return 8 + data.length;
    }

    protected void getContent(ByteBuffer byteBuffer) {
        byteBuffer.putInt(data.length + 8);
        IsoTypeWriter.writeUtf8String(byteBuffer, "data");
        byteBuffer.put(data);
    }


    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "appleDataBox=" + Hex.encodeHex(data) +
                '}';
    }

}
