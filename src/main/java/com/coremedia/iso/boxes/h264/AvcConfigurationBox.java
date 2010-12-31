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
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.BoxInterface;

import java.io.IOException;

/**
 * Defined in ISO/IEC 14496-15:2004.
 */
public final class AvcConfigurationBox extends Box {
    public static final String TYPE = "avcC";

    private int configurationVersion;
    private int avcProfileIndicaation;
    private int profileCompatibility;
    private int avcLevelIndication;
    private int lengthSizeMinusOne;
    private byte[] rest;

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

    public byte[] getRest() {
        return rest;
    }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, BoxInterface lastMovieFragmentBox) throws IOException {
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
        rest = in.read((int) size - 5);
    }

    protected long getContentSize() {
        return 5 + rest.length;
    }

    protected void getContent(IsoOutputStream os) throws IOException {
        os.writeUInt8(configurationVersion);
        os.writeUInt8(avcProfileIndicaation);
        os.writeUInt8(profileCompatibility);
        os.writeUInt8(avcLevelIndication);
        os.writeUInt8(lengthSizeMinusOne | (63 << 2));
        os.write(rest);
    }
}

