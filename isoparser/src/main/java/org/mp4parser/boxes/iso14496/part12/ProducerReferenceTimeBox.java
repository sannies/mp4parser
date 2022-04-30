/*
 * Copyright 2021 glboby27@gmail.com
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

package org.mp4parser.boxes.iso14496.part12;

import org.mp4parser.support.AbstractFullBox;
import org.mp4parser.tools.IsoTypeReader;
import org.mp4parser.tools.IsoTypeWriter;

import java.nio.ByteBuffer;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * <pre>
 * aligned(8) class ProducerReferenceTimeBox extends FullBox(‘prft’, version, 0) {
 *  unsigned int(32) reference_track_ID;
 *  unsigned int(64) ntp_timestamp;
 *  if (version==0) {
 *   unsigned int(32)  media_time;
 *  } else {
 *   unsigned int(64)  media_time;
 *  }
 * }
 * </pre>
 */
public class ProducerReferenceTimeBox extends AbstractFullBox {
    public static final String TYPE = "prft";
    private long referenceTrackId;
    private long ntpTimestamp;
    private long mediaTime;

    public ProducerReferenceTimeBox() { super(TYPE); }

    public long getReferenceTrackId() { return referenceTrackId; }

    public void setReferenceTrackId(long referenceTrackId) { this.referenceTrackId = referenceTrackId; }

    public long getNtpTimestamp() { return ntpTimestamp; }

    public void setNtpTimestamp(long ntpTimestamp) { this.ntpTimestamp = ntpTimestamp; }

    public long getMediaTime() { return mediaTime; }

    public void setMediaTime(long mediaTime) { this.mediaTime = mediaTime; }

    @Override
    protected long getContentSize() {
        return getVersion() == 0 ? 16 : 20;
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        setReferenceTrackId(IsoTypeReader.readUInt32(content));
        setNtpTimestamp(IsoTypeReader.readUInt64(content));
        if (getVersion() == 0) {
            setMediaTime(IsoTypeReader.readUInt32(content));
        } else {
            setMediaTime(IsoTypeReader.readUInt64(content));
        }
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter.writeUInt32(byteBuffer, getReferenceTrackId());
        IsoTypeWriter.writeUInt64(byteBuffer, getNtpTimestamp());
        if (getVersion() == 0) {
            IsoTypeWriter.writeUInt32(byteBuffer, getMediaTime());
        } else {
            IsoTypeWriter.writeUInt64(byteBuffer, getMediaTime());
        }
    }
}