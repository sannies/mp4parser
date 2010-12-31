package com.coremedia.iso.boxes;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;

import java.io.IOException;

/**
 * ES Descriptor Box.
 */
public class ESDescriptorBox extends FullBox {
    public static final String TYPE = "esds";


    private int eSDescriptorType;
    private int firstExtendedDescriptorTypeTag;
    private int secondExtendedDescriptorTypeTag;
    private int thirdExtendedDescriptorTypeTag;

    byte[] rest;

    public ESDescriptorBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    protected long getContentSize() {
        return 4 + rest.length;
    }

    public String getDisplayName() {
        return "ES Descriptor Box";
    }

    protected void getContent(IsoOutputStream os) throws IOException {
        os.writeUInt8(eSDescriptorType);
        os.writeUInt8(firstExtendedDescriptorTypeTag);
        os.writeUInt8(secondExtendedDescriptorTypeTag);
        os.writeUInt8(thirdExtendedDescriptorTypeTag);
        os.write(rest);
    }

    @Override
    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, BoxInterface lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        size -= 4; // header since we are dealing with a full box
        eSDescriptorType = in.readUInt8();
        firstExtendedDescriptorTypeTag = in.readUInt8();
        secondExtendedDescriptorTypeTag = in.readUInt8();
        thirdExtendedDescriptorTypeTag = in.readUInt8();
        size -= 4; // esDesc + first + second + third

        rest = in.read((int) size);
    }

    public int getESDescriptorType() {
        return eSDescriptorType;
    }

    public int getFirstExtendedDescriptorTypeTag() {
        return firstExtendedDescriptorTypeTag;
    }

    public int getSecondExtendedDescriptorTypeTag() {
        return secondExtendedDescriptorTypeTag;
    }

    public int getThirdExtendedDescriptorTypeTag() {
        return thirdExtendedDescriptorTypeTag;
    }
}
