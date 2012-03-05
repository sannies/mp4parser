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

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.coremedia.iso.boxes.AbstractBox;
import com.googlecode.mp4parser.h264.model.PictureParameterSet;
import com.googlecode.mp4parser.h264.model.SeqParameterSet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
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
        super(TYPE);
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

    public List<byte[]> getSequenceParameterSets() {
        return Collections.unmodifiableList(sequenceParameterSets);
    }

    public List<byte[]> getPictureParameterSets() {
        return Collections.unmodifiableList(pictureParameterSets);
    }

    public void setConfigurationVersion(int configurationVersion) {
        this.configurationVersion = configurationVersion;
    }

    public void setAvcProfileIndicaation(int avcProfileIndicaation) {
        this.avcProfileIndicaation = avcProfileIndicaation;
    }

    public void setProfileCompatibility(int profileCompatibility) {
        this.profileCompatibility = profileCompatibility;
    }

    public void setAvcLevelIndication(int avcLevelIndication) {
        this.avcLevelIndication = avcLevelIndication;
    }

    public void setLengthSizeMinusOne(int lengthSizeMinusOne) {
        this.lengthSizeMinusOne = lengthSizeMinusOne;
    }

    public void setSequenceParameterSets(List<byte[]> sequenceParameterSets) {
        this.sequenceParameterSets = sequenceParameterSets;
    }

    public void setPictureParameterSets(List<byte[]> pictureParameterSets) {
        this.pictureParameterSets = pictureParameterSets;
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        configurationVersion = IsoTypeReader.readUInt8(content);
        avcProfileIndicaation = IsoTypeReader.readUInt8(content);
        profileCompatibility = IsoTypeReader.readUInt8(content);
        avcLevelIndication = IsoTypeReader.readUInt8(content);
        int temp = IsoTypeReader.readUInt8(content);
        lengthSizeMinusOne = temp & 3;
        long numberOfSeuqenceParameterSets = IsoTypeReader.readUInt8(content) & 31;
        for (int i = 0; i < numberOfSeuqenceParameterSets; i++) {
            int sequenceParameterSetLength = IsoTypeReader.readUInt16(content);

            byte[] sequenceParameterSetNALUnit = new byte[sequenceParameterSetLength];
            content.get(sequenceParameterSetNALUnit);
            sequenceParameterSets.add(sequenceParameterSetNALUnit);
        }
        long numberOfPictureParameterSets = IsoTypeReader.readUInt8(content);
        for (int i = 0; i < numberOfPictureParameterSets; i++) {
            int pictureParameterSetLength = IsoTypeReader.readUInt16(content);
            byte[] pictureParameterSetNALUnit = new byte[pictureParameterSetLength];
            content.get(pictureParameterSetNALUnit);
            pictureParameterSets.add(pictureParameterSetNALUnit);
        }


    }


    public long getContentSize() {
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


    @Override
    public void getContent(ByteBuffer bb) throws IOException {
        IsoTypeWriter.writeUInt8(bb, configurationVersion);
        IsoTypeWriter.writeUInt8(bb, avcProfileIndicaation);
        IsoTypeWriter.writeUInt8(bb, profileCompatibility);
        IsoTypeWriter.writeUInt8(bb, avcLevelIndication);
        IsoTypeWriter.writeUInt8(bb, lengthSizeMinusOne | (63 << 2));
        IsoTypeWriter.writeUInt8(bb, (pictureParameterSets.size() & 31) | (7 << 5));
        for (byte[] sequenceParameterSetNALUnit : sequenceParameterSets) {
            IsoTypeWriter.writeUInt16(bb, sequenceParameterSetNALUnit.length);
            bb.put(sequenceParameterSetNALUnit);
        }
        IsoTypeWriter.writeUInt8(bb, pictureParameterSets.size());
        for (byte[] pictureParameterSetNALUnit : pictureParameterSets) {
            IsoTypeWriter.writeUInt16(bb, pictureParameterSetNALUnit.length);
            bb.put(pictureParameterSetNALUnit);
        }
    }


    // just to display sps in isoviewer no practical use
    public String[] getPPS() {
        ArrayList<String> l = new ArrayList<String>();
        for (byte[] pictureParameterSet : pictureParameterSets) {
            String details = "not parsable";
            try {
                details = PictureParameterSet.read(pictureParameterSet).toString();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            l.add(details);
        }
        return l.toArray(new String[l.size()]);
    }

    // just to display sps in isoviewer no practical use
    public String[] getSPS() {
        ArrayList<String> l = new ArrayList<String>();
        for (byte[] sequenceParameterSet : sequenceParameterSets) {
            String detail = "not parsable";
            try {
                detail = SeqParameterSet.read(new ByteArrayInputStream(sequenceParameterSet)).toString();
            } catch (IOException e) {

            }
            l.add(detail);
        }
        return l.toArray(new String[l.size()]);
    }


}

