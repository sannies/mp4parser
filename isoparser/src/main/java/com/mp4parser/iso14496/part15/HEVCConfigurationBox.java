package com.mp4parser.iso14496.part15;

import com.googlecode.mp4parser.AbstractBox;

import java.nio.ByteBuffer;

/**
 * Created by sannies on 08.09.2014.
 */
public class HEVCConfigurationBox extends AbstractBox {
    public static final String TYPE = "hvcC";

    private HEVCDecoderConfigurationRecord hevcDecoderConfigurationRecord;

    public HEVCConfigurationBox() {
        super(TYPE);
    }

    @Override
    protected long getContentSize() {
        return hevcDecoderConfigurationRecord.getSize();
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        hevcDecoderConfigurationRecord.write(byteBuffer);
    }

    @Override
    protected void _parseDetails(ByteBuffer content) {
        hevcDecoderConfigurationRecord = new HEVCDecoderConfigurationRecord();
        hevcDecoderConfigurationRecord.parse(content);
    }

    public HEVCDecoderConfigurationRecord getHevcDecoderConfigurationRecord() {
        return hevcDecoderConfigurationRecord;
    }

    public void setHevcDecoderConfigurationRecord(HEVCDecoderConfigurationRecord hevcDecoderConfigurationRecord) {
        this.hevcDecoderConfigurationRecord = hevcDecoderConfigurationRecord;
    }
}
