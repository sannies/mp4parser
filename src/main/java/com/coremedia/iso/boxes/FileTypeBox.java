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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * This box identifies the specifications to which this file complies. <br>
 * Each brand is a printable four-character code, registered with ISO, that
 * identifies a precise specification.
 */
public class FileTypeBox extends AbstractBox {
    public static final String TYPE = "ftyp";

    private String majorBrand;
    private long minorVersion;
    private Collection<String> compatibleBrands;

    public FileTypeBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    public FileTypeBox(String majorBrand, long minorVersion, Collection<String> compatibleBrands) {
        super(IsoFile.fourCCtoBytes(TYPE));
        this.majorBrand = majorBrand;
        this.minorVersion = minorVersion;
        this.compatibleBrands = compatibleBrands;
    }

    protected long getContentSize() {
        return 8 + compatibleBrands.size() * 4;

    }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        majorBrand = IsoFile.bytesToFourCC(in.read(4));
        minorVersion = in.readUInt32();
        int compatibleBrandsCount = (int) ((size - 8) / 4);
        compatibleBrands = new LinkedList<String>();
        for (int i = 0; i < compatibleBrandsCount; i++) {
            compatibleBrands.add(IsoFile.bytesToFourCC(in.read(4)));
        }
    }

    protected void getContent(IsoOutputStream isos) throws IOException {
        isos.write(IsoFile.fourCCtoBytes(majorBrand));
        isos.writeUInt32(minorVersion);
        for (String compatibleBrand : compatibleBrands) {
            isos.write(IsoFile.fourCCtoBytes(compatibleBrand));
        }

    }

    /**
     * Gets the brand identifier.
     *
     * @return the brand identifier
     */
    public String getMajorBrand() {
        return majorBrand;
    }

    /**
     * Sets the major brand of the file used to determine an appropriate reader.
     *
     * @param majorBrand the new major brand
     */
    public void setMajorBrand(String majorBrand) {
        this.majorBrand = majorBrand;
    }

    /**
     * Sets the "informative integer for the minor version of the major brand".
     *
     * @param minorVersion the version number of the major brand
     */
    public void setMinorVersion(int minorVersion) {
        this.minorVersion = minorVersion;
    }

    /**
     * Gets an informative integer for the minor version of the major brand.
     *
     * @return an informative integer
     * @see FileTypeBox#getMajorBrand()
     */
    public long getMinorVersion() {
        return minorVersion;
    }

    /**
     * Gets an array of 4-cc brands.
     *
     * @return the compatible brands
     */
    public Collection<String> getCompatibleBrands() {
        return compatibleBrands;
    }

    public void setCompatibleBrands(List<String> compatibleBrands) {
        this.compatibleBrands = compatibleBrands;
    }

    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("FileTypeBox[");
        result.append("majorBrand=").append(getMajorBrand());
        result.append(";");
        result.append("minorVersion=").append(getMinorVersion());
        for (String compatibleBrand : compatibleBrands) {
            result.append(";");
            result.append("compatibleBrand=").append(compatibleBrand);
        }
        result.append("]");
        return result.toString();
    }
}
