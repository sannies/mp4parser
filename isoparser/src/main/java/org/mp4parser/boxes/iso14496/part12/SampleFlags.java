/*
 * Copyright 2009 castLabs GmbH, Berlin
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
 * bit(6) reserved=0;
 * unsigned int(2) sample_depends_on;
 * unsigned int(2) sample_is_depended_on;
 * unsigned int(2) sample_has_redundancy;
 * bit(3) sample_padding_value;
 * bit(1) sample_is_difference_sample;
 * // i.e. when 1 signals a non-key or non-sync sample
 * unsigned int(16) sample_degradation_priority;
 */
public class SampleFlags {
    private byte reserved;
    private byte isLeading;
    private byte sampleDependsOn;
    private byte sampleIsDependedOn;
    private byte sampleHasRedundancy;
    private byte samplePaddingValue;
    private boolean sampleIsDifferenceSample;
    private int sampleDegradationPriority;

    public SampleFlags() {

    }

    public SampleFlags(ByteBuffer bb) {
        long a = IsoTypeReader.readUInt32(bb);
        reserved = (byte) ((a & 0xF0000000) >> 28);
        isLeading = (byte) ((a & 0x0C000000) >> 26);
        sampleDependsOn = (byte) ((a & 0x03000000) >> 24);
        sampleIsDependedOn = (byte) ((a & 0x00C00000) >> 22);
        sampleHasRedundancy = (byte) ((a & 0x00300000) >> 20);
        samplePaddingValue = (byte) ((a & 0x000e0000) >> 17);
        sampleIsDifferenceSample = ((a & 0x00010000) >> 16) > 0;
        sampleDegradationPriority = (int) (a & 0x0000ffff);

    }


    public void getContent(ByteBuffer os) {
        long a = 0;
        a |= reserved << 28;
        a |= isLeading << 26;
        a |= sampleDependsOn << 24;
        a |= sampleIsDependedOn << 22;
        a |= sampleHasRedundancy << 20;
        a |= samplePaddingValue << 17;
        a |= (sampleIsDifferenceSample ? 1 : 0) << 16;
        a |= sampleDegradationPriority;
        IsoTypeWriter.writeUInt32(os, a);
    }

    public int getReserved() {
        return reserved;
    }

    public void setReserved(int reserved) {
        this.reserved = (byte) reserved;
    }

    public byte getIsLeading() {
        return isLeading;
    }

    public void setIsLeading(byte isLeading) {
        this.isLeading = isLeading;
    }

    /**
     * sample_depends_on takes one of the following four values:
     * <pre>
     * 0: the dependency of this sample is unknown;
     * 1: this sample does depend on others (not an I picture);
     * 2: this sample does not depend on others (I picture);
     * 3: reserved
     * </pre>
     *
     * @return current depends_on level
     */
    public int getSampleDependsOn() {
        return sampleDependsOn;
    }

    /**
     * sample_depends_on takes one of the following four values:
     * <pre>
     * 0: the dependency of this sample is unknown;
     * 1: this sample does depend on others (not an I picture);
     * 2: this sample does not depend on others (I picture);
     * 3: reserved
     * </pre>
     *
     * @param sampleDependsOn new depends on value
     */
    public void setSampleDependsOn(int sampleDependsOn) {
        this.sampleDependsOn = (byte) sampleDependsOn;
    }

    /**
     * sample_is_depended_on takes one of the following four values:
     * <pre>
     * 0: the dependency of other samples on this sample is unknown;
     * 1: other samples may depend on this one (not disposable);
     * 2: no other sample depends on this one (disposable);
     * 3: reserved
     * </pre>
     *
     * @return current is_dependend_on level
     */
    public int getSampleIsDependedOn() {
        return sampleIsDependedOn;
    }

    /**
     * sample_is_depended_on takes one of the following four values:
     * <pre>
     * 0: the dependency of other samples on this sample is unknown;
     * 1: other samples may depend on this one (not disposable);
     * 2: no other sample depends on this one (disposable);
     * 3: reserved
     * </pre>
     *
     * @param sampleIsDependedOn new is_depends on value
     */
    public void setSampleIsDependedOn(int sampleIsDependedOn) {
        this.sampleIsDependedOn = (byte) sampleIsDependedOn;
    }

    /**
     * sample_has_redundancy takes one of the following four values:
     * <pre>
     * 0: it is unknown whether there is redundant coding in this sample;
     * 1: there is redundant coding in this sample;
     * 2: there is no redundant coding in this sample;
     * 3: reserved
     * </pre>
     *
     * @return current redundancy level
     */
    public int getSampleHasRedundancy() {
        return sampleHasRedundancy;
    }

    /**
     * sample_has_redundancy takes one of the following four values:
     * <pre>
     * 0: it is unknown whether there is redundant coding in this sample;
     * 1: there is redundant coding in this sample;
     * 2: there is no redundant coding in this sample;
     * 3: reserved
     * </pre>
     *
     * @param sampleHasRedundancy new redundancy level
     */
    public void setSampleHasRedundancy(int sampleHasRedundancy) {
        this.sampleHasRedundancy = (byte) sampleHasRedundancy;
    }

    public int getSamplePaddingValue() {
        return samplePaddingValue;
    }

    public void setSamplePaddingValue(int samplePaddingValue) {
        this.samplePaddingValue = (byte) samplePaddingValue;
    }

    public boolean isSampleIsDifferenceSample() {
        return sampleIsDifferenceSample;
    }


    public void setSampleIsDifferenceSample(boolean sampleIsDifferenceSample) {
        this.sampleIsDifferenceSample = sampleIsDifferenceSample;
    }

    public int getSampleDegradationPriority() {
        return sampleDegradationPriority;
    }

    public void setSampleDegradationPriority(int sampleDegradationPriority) {
        this.sampleDegradationPriority = sampleDegradationPriority;
    }

    @Override
    public String toString() {
        return "SampleFlags{" +
                "reserved=" + reserved +
                ", isLeading=" + isLeading +
                ", depOn=" + sampleDependsOn +
                ", isDepOn=" + sampleIsDependedOn +
                ", hasRedundancy=" + sampleHasRedundancy +
                ", padValue=" + samplePaddingValue +
                ", isDiffSample=" + sampleIsDifferenceSample +
                ", degradPrio=" + sampleDegradationPriority +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SampleFlags that = (SampleFlags) o;

        if (isLeading != that.isLeading) return false;
        if (reserved != that.reserved) return false;
        if (sampleDegradationPriority != that.sampleDegradationPriority) return false;
        if (sampleDependsOn != that.sampleDependsOn) return false;
        if (sampleHasRedundancy != that.sampleHasRedundancy) return false;
        if (sampleIsDependedOn != that.sampleIsDependedOn) return false;
        if (sampleIsDifferenceSample != that.sampleIsDifferenceSample) return false;
        if (samplePaddingValue != that.samplePaddingValue) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) reserved;
        result = 31 * result + (int) isLeading;
        result = 31 * result + (int) sampleDependsOn;
        result = 31 * result + (int) sampleIsDependedOn;
        result = 31 * result + (int) sampleHasRedundancy;
        result = 31 * result + (int) samplePaddingValue;
        result = 31 * result + (sampleIsDifferenceSample ? 1 : 0);
        result = 31 * result + sampleDegradationPriority;
        return result;
    }
}
