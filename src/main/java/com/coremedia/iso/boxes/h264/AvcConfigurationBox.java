/*  
 * Copyright 2008 CoreMedia AG, Hamburg
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

package com.coremedia.iso.boxes.h264;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.AbstractBox;
import com.coremedia.iso.boxes.Box;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Defined in ISO/IEC 14496-15:2004.
 */
public final class AvcConfigurationBox extends AbstractBox {
    public static final String TYPE = "avcC";

    private int configurationVersion;
    private int avcProfileIndicaation;
    private int profileCompatibility;
    private int avcLevelIndication;
    private int lengthSizeMinusOne;
    List<byte[]> sequenceParameterSets = new ArrayList<byte[]>();
    List<byte[]> pictureParameterSets = new ArrayList<byte[]>();


    public AvcConfigurationBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    public int getConfigurationVersion() {
        return configurationVersion;
    }

    public int getAvcProfileIndicaation() {
        return avcProfileIndicaation;
    }

    public int getProfileCompatibility() {
        return profileCompatibility;
    }

    public int getAvcLevelIndication() {
        return avcLevelIndication;
    }

    public int getLengthSizeMinusOne() {
        return lengthSizeMinusOne;
    }

    public String getDisplayName() {
        return "AVC Configuration Box";
    }


    public List<byte[]> getSequenceParameterSets() {
        return Collections.unmodifiableList(sequenceParameterSets);
    }

    public List<byte[]> getPictureParameterSets() {
        return Collections.unmodifiableList(pictureParameterSets);
    }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        /*
     unsigned int(8) configurationVersion = 1;
     unsigned int(8) AVCProfileIndication;
     unsigned int(8) profile_compatibility;
     unsigned int(8) AVCLevelIndication;
     bit(6) reserved = '111111'b;
     unsigned int(2) lengthSizeMinusOne;
     bit(3) reserved = '111'b;

        */
        configurationVersion = in.readUInt8();
        avcProfileIndicaation = in.readUInt8();
        profileCompatibility = in.readUInt8();
        avcLevelIndication = in.readUInt8();
        int temp = in.readUInt8();
        lengthSizeMinusOne = temp & 3;
        long numberOfSeuqenceParameterSets = in.readUInt8() & 31;
        for (int i = 0; i < numberOfSeuqenceParameterSets; i++) {
            int sequenceParameterSetLength = in.readUInt16();
            byte[] sequenceParameterSetNALUnit = in.read(sequenceParameterSetLength);
            sequenceParameterSets.add(sequenceParameterSetNALUnit);
        }
        long numberOfPictureParameterSets = in.readUInt8();
        for (int i = 0; i < numberOfPictureParameterSets; i++) {
            int pictureParameterSetLength = in.readUInt16();
            byte[] pictureParameterSetNALUnit = in.read(pictureParameterSetLength);
            pictureParameterSets.add(pictureParameterSetNALUnit);
        }


    }

    protected long getContentSize() {
        long size = 5;
        size += 1; // sequenceParamsetLength
        for (byte[] sequenceParameterSetNALUnit : sequenceParameterSets) {
            size += 2; //lengthSizeMinusOne field
            size += sequenceParameterSetNALUnit.length;
        }
        size += 1; // pictureParamsetLength
        for (byte[] pictureParameterSetNALUnit : pictureParameterSets) {
            size += 2; //lengthSizeMinusOne field
            size += pictureParameterSetNALUnit.length;
        }

        return size;
    }

    protected void getContent(IsoOutputStream os) throws IOException {
        os.writeUInt8(configurationVersion);
        os.writeUInt8(avcProfileIndicaation);
        os.writeUInt8(profileCompatibility);
        os.writeUInt8(avcLevelIndication);
        os.writeUInt8(lengthSizeMinusOne | (63 << 2));
        os.writeUInt8((pictureParameterSets.size() & 31) | (7 << 5));
        for (byte[] sequenceParameterSetNALUnit : sequenceParameterSets) {
            os.writeUInt16(sequenceParameterSetNALUnit.length);
            os.write(sequenceParameterSetNALUnit);
        }
        os.writeUInt8(pictureParameterSets.size());
        for (byte[] pictureParameterSetNALUnit : pictureParameterSets) {
            os.writeUInt16(pictureParameterSetNALUnit.length);
            os.write(pictureParameterSetNALUnit);
        }
    }
}

