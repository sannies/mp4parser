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

package com.coremedia.iso.boxes.fragment;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.AbstractFullBox;
import com.coremedia.iso.boxes.Box;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * aligned(8) class SampleDependencyTypeBox
 * extends FullBox('sdtp', version = 0, 0) {
 * for (i=0; i < sample_count; i++){
 * unsigned int(2) reserved = 0;
 * unsigned int(2) sample_depends_on;
 * unsigned int(2) sample_is_depended_on;
 * unsigned int(2) sample_has_redundancy;
 * }
 * }
 */
public class SampleDependencyTypeBox extends AbstractFullBox {
    public static final String TYPE = "sdtp";

    private List<Entry> entries = new ArrayList<Entry>();

    public static class Entry {
        private int reserved;
        private int sampleDependsOn;
        private int sampleIsDependentOn;
        private int sampleHasRedundancy;

        public int getReserved() {
            return reserved;
        }

        public int getSampleDependsOn() {
            return sampleDependsOn;
        }

        public int getSampleIsDependentOn() {
            return sampleIsDependentOn;
        }

        public int getSampleHasRedundancy() {
            return sampleHasRedundancy;
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "reserved=" + reserved +
                    ", sampleDependsOn=" + sampleDependsOn +
                    ", sampleIsDependentOn=" + sampleIsDependentOn +
                    ", sampleHasRedundancy=" + sampleHasRedundancy +
                    '}';
        }
    }

    public SampleDependencyTypeBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    @Override
    public String getDisplayName() {
        return "Independent and Disposable Samples Box";
    }

    @Override
    protected long getContentSize() {
        return entries.size();
    }

    @Override
    protected void getContent(IsoOutputStream os) throws IOException {
        for (Entry entry : entries) {
            int temp = entry.reserved << 6;

            temp = temp | (entry.sampleDependsOn << 4);
            temp = temp | (entry.sampleIsDependentOn << 2);
            temp = temp | entry.sampleHasRedundancy;

            os.write(temp);
        }
    }

    @Override
    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        long remainingBytes = size - 4;

        while (remainingBytes > 0) {
            Entry entry = new Entry();
            int temp = in.readUInt8();

            entry.reserved = temp >> 6;
            entry.sampleDependsOn = (temp >> 4) & 0x3;
            entry.sampleIsDependentOn = (temp >> 2) & 0x3;
            entry.sampleHasRedundancy = temp & 0x3;

            entries.add(entry);
            remainingBytes--;
        }

    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("SampleDependencyTypeBox");
        sb.append("{entries=").append(entries);
        sb.append('}');
        return sb.toString();
    }
}
