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

package com.coremedia.iso.boxes.odf;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.AbstractContainerBox;
import com.coremedia.iso.boxes.Box;

/**
 * A mutable DRM information box may appear in both DCF and PDCF. In the OMA DRM system,
 * <code>MutableDrmInformationBox</code> is used to include information editable by the
 * device, and thus is protected for integrity. A device MUST ignore the box when
 * calculating the DCF Hash.
 */
public class MutableDrmInformationBox extends AbstractContainerBox {
    public static final String TYPE = "mdri";

    public MutableDrmInformationBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    public String getDisplayName() {
        return "Mutable DRM Information Box";
    }

}
