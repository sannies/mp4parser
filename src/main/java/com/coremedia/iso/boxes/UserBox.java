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

package com.coremedia.iso.boxes;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;

import java.io.IOException;

/**
 * A user specifc box. See ISO/IEC 14496-12 for details.
 */
public class UserBox extends Box {
    byte[] content;
    public static final String TYPE = "uuid";

    public UserBox(byte[] userType) {
        super(IsoFile.fourCCtoBytes(TYPE));
        setUserType(userType);
    }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, BoxInterface lastMovieFragmentBox) throws IOException {
        if (((int) size) != size) {
            throw new RuntimeException("The UserBox cannot be larger than 2^32 bytes (Plz enhance the parser!!!)");
        }
        content = in.read((int) size);
    }

    public String getDisplayName() {
        return "User Box " + new String(getUserType());
    }

    protected long getContentSize() {
        return content.length;
    }

    public String toString() {
        return "UserBox[type=" + IsoFile.bytesToFourCC(getType()) +
                ";userType=" + new String(getUserType()) +
                ";contentLength=" + content.length + "]";
    }

    protected void getContent(IsoOutputStream os) throws IOException {
        os.write(content);
    }

    public byte[] getBox() {
        return content;
    }
}
