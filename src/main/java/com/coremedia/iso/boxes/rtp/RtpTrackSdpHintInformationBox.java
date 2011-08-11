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

package com.coremedia.iso.boxes.rtp;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.assistui.multiline;
import com.coremedia.iso.boxes.AbstractBox;
import com.coremedia.iso.boxes.Box;

import java.io.IOException;

/**
 * Contains the information that will be used by the streaming server to create the SDP file.
 */
public class RtpTrackSdpHintInformationBox extends AbstractBox {
    public static final String TYPE = "sdp ";

    @multiline(linebreak = "\r\n")
    private String sdpText;


    protected long getContentSize() {
        return utf8StringLengthInBytes(sdpText);
    }

    public RtpTrackSdpHintInformationBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    public String getSdpText() {
        return sdpText;
    }

    public void setSdpText(String sdpText) {
        this.sdpText = sdpText;
    }

    @Override
    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        sdpText = new String(in.read((int) size), "UTF-8");
    }

    public String toString() {
        return "RtpTrackSdpHintInformationBox[sdpText=" + getSdpText() + "]";
    }

    protected void getContent(IsoOutputStream os) throws IOException {
        os.writeStringNoTerm(sdpText);
    }
}
