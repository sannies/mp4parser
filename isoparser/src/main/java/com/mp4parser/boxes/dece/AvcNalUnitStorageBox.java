/*
 * Copyright 2011 castLabs, Berlin
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mp4parser.boxes.dece;

import com.mp4parser.boxes.iso14496.part15.AvcConfigurationBox;
import com.mp4parser.support.AbstractBox;
import com.mp4parser.boxes.iso14496.part15.AvcDecoderConfigurationRecord;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * The AVC NAL Unit Storage Box SHALL contain an AVCDecoderConfigurationRecord,
 * as defined in section 5.2.4.1 of the ISO 14496-12.
 */
public class AvcNalUnitStorageBox extends AbstractBox {
    public static final String TYPE = "avcn";
    AvcDecoderConfigurationRecord avcDecoderConfigurationRecord;

    public AvcNalUnitStorageBox() {
        super(TYPE);
    }

    public AvcNalUnitStorageBox(AvcConfigurationBox avcConfigurationBox) {
        super(TYPE);
        this.avcDecoderConfigurationRecord = avcConfigurationBox.getavcDecoderConfigurationRecord();
    }

    public AvcDecoderConfigurationRecord getAvcDecoderConfigurationRecord() {
        return avcDecoderConfigurationRecord;
    }

    // just to display sps in isoviewer no practical use
    public int getLengthSizeMinusOne() {
        return avcDecoderConfigurationRecord.lengthSizeMinusOne;
    }

    public List<String> getSequenceParameterSetsAsStrings() {
        return avcDecoderConfigurationRecord.getSequenceParameterSetsAsStrings();
    }

    public List<String> getSequenceParameterSetExtsAsStrings() {
        return avcDecoderConfigurationRecord.getSequenceParameterSetExtsAsStrings();
    }

    public List<String> getPictureParameterSetsAsStrings() {
        return avcDecoderConfigurationRecord.getPictureParameterSetsAsStrings();
    }

    @Override
    protected long getContentSize() {
        return avcDecoderConfigurationRecord.getContentSize();
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        this.avcDecoderConfigurationRecord = new AvcDecoderConfigurationRecord(content);
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        this.avcDecoderConfigurationRecord.getContent(byteBuffer);
    }

    @Override
    public String toString() {
        return "AvcNalUnitStorageBox{" +
                "SPS=" + avcDecoderConfigurationRecord.getSequenceParameterSetsAsStrings() +
                ",PPS=" + avcDecoderConfigurationRecord.getPictureParameterSetsAsStrings() +
                ",lengthSize=" + (avcDecoderConfigurationRecord.lengthSizeMinusOne + 1) +
                '}';
    }
}
