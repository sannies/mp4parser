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
 * This box identifies the specifications to which this file complies. <br>
 * Each brand is a printable four-character code, registered with ISO, that
 * identifies a precise specification.
 */
public class FileTypeBox extends AbstractBox {
    public static final String TYPE = "ftyp";

    private String majorBrand;
    private long minorVerson;
    private String[] compatibleBrands;

    public FileTypeBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    protected long getContentSize() {
        return 8 + compatibleBrands.length * 4;

    }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        majorBrand = IsoFile.bytesToFourCC(in.read(4));
        minorVerson = in.readUInt32();
        int compatibleBrandsCount = (int) ((size - 8) / 4);
        compatibleBrands = new String[compatibleBrandsCount];
        for (int i = 0; i < compatibleBrandsCount; i++) {
            compatibleBrands[i] = IsoFile.bytesToFourCC(in.read(4));
        }
    }

    protected void getContent(IsoOutputStream isos) throws IOException {
        isos.write(IsoFile.fourCCtoBytes(majorBrand));
        isos.writeUInt32(minorVerson);
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
     * @param minorVerson the version number of the major brand
     */
    public void setMinorVerson(int minorVerson) {
        this.minorVerson = minorVerson;
    }

    /**
     * Gets an informative integer for the minor version of the major brand.
     *
     * @return an informative integer
     * @see FileTypeBox#getMajorBrand()
     */
    public long getMinorVerson() {
        return minorVerson;
    }

    /**
     * Gets an array of 4-cc brands.
     *
     * @return the compatible brands
     */
    public String[] getCompatibleBrands() {
        return compatibleBrands;
    }

    public void setCompatibleBrands(String[] compatibleBrands) {
        this.compatibleBrands = compatibleBrands;
    }


    public String getDisplayName() {
        return "File Type Box";
    }

    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("FileTypeBox[");
        result.append("majorBrand=").append(getMajorBrand());
        result.append(";");
        result.append("minorVerson=").append(getMinorVerson());
        for (String compatibleBrand : compatibleBrands) {
            result.append(";");
            result.append("compatibleBrand=").append(compatibleBrand);
        }
        result.append("]");
        return result.toString();
    }
}
