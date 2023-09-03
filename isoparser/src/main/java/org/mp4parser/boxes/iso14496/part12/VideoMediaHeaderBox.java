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

package org.mp4parser.boxes.iso14496.part12;

import org.mp4parser.tools.IsoTypeReader;
import org.mp4parser.tools.IsoTypeWriter;

import java.nio.ByteBuffer;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * The video media header contains general presentation information, independent of the coding, for video
 * media. Note that the flags field has the value 1.
 */
public class VideoMediaHeaderBox extends AbstractMediaHeaderBox {
    public static final String TYPE = "vmhd";
    private int graphicsmode = 0;
    private int[] opcolor = new int[]{0, 0, 0};

    public VideoMediaHeaderBox() {
        super(TYPE);
        this.flags = 1;
    }

    public int getGraphicsmode() {
        return graphicsmode;
    }

    public void setGraphicsmode(int graphicsmode) {
        this.graphicsmode = graphicsmode;
    }

    public int[] getOpcolor() {
        return opcolor;
    }

    public void setOpcolor(int[] opcolor) {
        this.opcolor = opcolor;
    }

    protected long getContentSize() {
        return 12;
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        graphicsmode = IsoTypeReader.readUInt16(content);
        opcolor = new int[3];
        for (int i = 0; i < 3; i++) {
            opcolor[i] = IsoTypeReader.readUInt16(content);
        }
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter.writeUInt16(byteBuffer, graphicsmode);
        for (int anOpcolor : opcolor) {
            IsoTypeWriter.writeUInt16(byteBuffer, anOpcolor);
        }
    }

    public String toString() {
        return "VideoMediaHeaderBox[graphicsmode=" + getGraphicsmode() + ";opcolor0=" + getOpcolor()[0] + ";opcolor1=" + getOpcolor()[1] + ";opcolor2=" + getOpcolor()[2] + "]";
    }
}
