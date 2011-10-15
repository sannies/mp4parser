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

package com.googlecode.mp4parser.boxes.mp4.objectdescriptors;

import com.coremedia.iso.IsoBufferWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/*
class ES_Descriptor extends BaseDescriptor : bit(8) tag=ES_DescrTag {
bit(16) ES_ID;
bit(1) streamDependenceFlag;
bit(1) URL_Flag;
bit(1) OCRstreamFlag;
bit(5) streamPriority;
if (streamDependenceFlag)
bit(16) dependsOn_ES_ID;
if (URL_Flag) {
bit(8) URLlength;
bit(8) URLstring[URLlength];
}
if (OCRstreamFlag)
bit(16) OCR_ES_Id;
DecoderConfigDescriptor decConfigDescr;
if (ODProfileLevelIndication==0x01) //no SL extension.
{
SLConfigDescriptor slConfigDescr;
}
else // SL extension is possible.
{
SLConfigDescriptor slConfigDescr;
}
IPI_DescrPointer ipiPtr[0 .. 1];
IP_IdentificationDataSet ipIDS[0 .. 255];
IPMP_DescriptorPointer ipmpDescrPtr[0 .. 255];
LanguageDescriptor langDescr[0 .. 255];
QoS_Descriptor qosDescr[0 .. 1];
RegistrationDescriptor regDescr[0 .. 1];
ExtensionDescriptor extDescr[0 .. 255];
}
 */
@Descriptor(tags = {0x03})
public class ESDescriptor extends BaseDescriptor {
    private static Logger log = Logger.getLogger(ESDescriptor.class.getName());

    int esId;
    int streamDependenceFlag;
    int URLFlag;
    int oCRstreamFlag;
    int streamPriority;


    int URLLength = 0;
    String URLString;
    int remoteODFlag;

    int dependsOnEsId;
    int oCREsId;

    DecoderConfigDescriptor decoderConfigDescriptor;
    SLConfigDescriptor slConfigDescriptor;
    List<BaseDescriptor> otherDescriptors = new ArrayList<BaseDescriptor>();

    @Override
    public void parse(int tag, IsoBufferWrapper in, int maxLength) throws IOException {
        super.parse(tag, in, maxLength);

        esId = in.readUInt16();

        int data = in.readUInt8();
        streamDependenceFlag = data >>> 7;
        URLFlag = (data >>> 6) & 0x1;
        oCRstreamFlag = (data >>> 5) & 0x1;
        streamPriority = data & 0x1f;

        if (streamDependenceFlag == 1) {
            dependsOnEsId = in.readUInt16();
        }
        if (URLFlag == 1) {
            URLLength = in.readUInt8();
            URLString = new String(in.read(URLLength));
        }
        if (oCRstreamFlag == 1) {
            oCREsId = in.readUInt16();
        }

        int baseSize = 1 /*tag*/ + getSizeBytes() + 2 + 1 + (streamDependenceFlag == 1 ? 2 : 0) + (URLFlag == 1 ? 1 + URLLength : 0) + (oCRstreamFlag == 1 ? 2 : 0);

        long begin = in.position();
        if (getSize() > baseSize + 2) {
            BaseDescriptor descriptor = ObjectDescriptorFactory.createFrom(-1, in, getSize() - baseSize);
            final long read = in.position() - begin;
            log.finer(descriptor + " - ESDescriptor1 read: " + read + ", size: " + (descriptor != null ? descriptor.getSize() : null));
            if (descriptor != null) {
                final int size = descriptor.getSize();
                in.position(begin + size);
                baseSize += size;
            } else {
                baseSize += read;
            }
            if (descriptor instanceof DecoderConfigDescriptor) {
                decoderConfigDescriptor = (DecoderConfigDescriptor) descriptor;
            }
        }

        begin = in.position();
        if (getSize() > baseSize + 2) {
            BaseDescriptor descriptor = ObjectDescriptorFactory.createFrom(-1, in, getSize() - baseSize);
            final long read = in.position() - begin;
            log.finer(descriptor + " - ESDescriptor2 read: " + read + ", size: " + (descriptor != null ? descriptor.getSize() : null));
            if (descriptor != null) {
                final int size = descriptor.getSize();
                in.position(begin + size);
                baseSize += size;
            } else {
                baseSize += read;
            }
            if (descriptor instanceof SLConfigDescriptor) {
                slConfigDescriptor = (SLConfigDescriptor) descriptor;
            }
        } else {
            log.warning("SLConfigDescriptor is missing!");
        }

        while (getSize() - baseSize > 2) {
            begin = in.position();
            BaseDescriptor descriptor = ObjectDescriptorFactory.createFrom(-1, in, getSize() - baseSize);
            final long read = in.position() - begin;
            log.finer(descriptor + " - ESDescriptor3 read: " + read + ", size: " + (descriptor != null ? descriptor.getSize() : null));
            if (descriptor != null) {
                final int size = descriptor.getSize();
                in.position(begin + size);
                baseSize += size;
            } else {
                baseSize += read;
            }
            otherDescriptors.add(descriptor);
        }
    }

//  @Override
//  public int getSize() {
//    return 3 + (streamDependenceFlag == 1 ? 2 : 0) +
//            (URLFlag == 1 ? 1 + 8 * URLLength : 0) +
//            (oCRstreamFlag == 1 ? 2 : 0);
//  }

    public DecoderConfigDescriptor getDecoderConfigDescriptor() {
        return decoderConfigDescriptor;
    }

    public SLConfigDescriptor getSlConfigDescriptor() {
        return slConfigDescriptor;
    }

    public List<BaseDescriptor> getOtherDescriptors() {
        return otherDescriptors;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ESDescriptor");
        sb.append("{esId=").append(esId);
        sb.append(", streamDependenceFlag=").append(streamDependenceFlag);
        sb.append(", URLFlag=").append(URLFlag);
        sb.append(", oCRstreamFlag=").append(oCRstreamFlag);
        sb.append(", streamPriority=").append(streamPriority);
        sb.append(", URLLength=").append(URLLength);
        sb.append(", URLString='").append(URLString).append('\'');
        sb.append(", remoteODFlag=").append(remoteODFlag);
        sb.append(", dependsOnEsId=").append(dependsOnEsId);
        sb.append(", oCREsId=").append(oCREsId);
        sb.append(", decoderConfigDescriptor=").append(decoderConfigDescriptor);
        sb.append(", slConfigDescriptor=").append(slConfigDescriptor);
        sb.append('}');
        return sb.toString();
    }
}
