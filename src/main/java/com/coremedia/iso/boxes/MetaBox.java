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

import com.googlecode.mp4parser.AbstractBox;
import com.googlecode.mp4parser.AbstractContainerBox;

import java.nio.ByteBuffer;


/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * A common base structure to contain general metadata. See ISO/IEC 14496-12 Ch. 8.44.1.
 */
public class MetaBox extends AbstractContainerBox {
    public static final String TYPE = "meta";

    public MetaBox() {
        super(TYPE);
    }
}
