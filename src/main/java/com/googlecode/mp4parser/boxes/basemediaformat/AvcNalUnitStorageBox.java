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

package com.googlecode.mp4parser.boxes.basemediaformat;

import com.coremedia.iso.boxes.AbstractBox;
import com.coremedia.iso.boxes.h264.AvcConfigurationBox;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.coremedia.iso.boxes.CastUtils.l2i;

/**
 * The AVC NAL Unit Storage Box SHALL contain an AVCDecoderConfigurationRecord,
 * as defined in section 5.2.4.1 of the ISO 14496-12.
 */
public class AvcNalUnitStorageBox extends AbstractBox {
    byte[] data;

    public AvcNalUnitStorageBox() {
        super("avcn");
    }


    public AvcNalUnitStorageBox(AvcConfigurationBox avcConfigurationBox) {
        super("avcn");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        ByteBuffer content = ByteBuffer.allocate(l2i(avcConfigurationBox.getContentSize()));
        try {
            avcConfigurationBox.getContent(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        data = content.array();
    }

    @Override
    protected long getContentSize() {
        return data.length;
    }


    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        data = new byte[content.remaining()];
    }

    @Override
    protected void getContent(ByteBuffer bb) throws IOException {
        bb.put(data);
    }

    @Override
    public String toString() {
        return "AvcNalUnitStorageBox{" +
                "data=" + Arrays.toString(data) +
                '}';
    }
}
