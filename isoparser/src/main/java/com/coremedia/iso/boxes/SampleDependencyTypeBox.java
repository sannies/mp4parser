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

package com.coremedia.iso.boxes;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.googlecode.mp4parser.AbstractFullBox;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * <pre>
 * aligned(8) class SampleDependencyTypeBox extends FullBox('sdtp', version = 0, 0) {
 *  for (i=0; i &lt; sample_count; i++){
 *   unsigned int(2) isLeading;
 *   unsigned int(2) sample_depends_on;
 *   unsigned int(2) sample_is_depended_on;
 *   unsigned int(2) sample_has_redundancy;
 *  }
 * }
 * </pre>
 */
public class SampleDependencyTypeBox extends AbstractFullBox {
    public static final String TYPE = "sdtp";

    private List<Entry> entries = new ArrayList<Entry>();

    public static class Entry {

        public Entry(int value) {
            this.value = value;
        }

        private int value;


        public int getIsLeading() {
            return (value >> 6) & 0x03;
        }

        public void setIsLeading(int res) {
            value = (res & 0x03) << 6 | value & 0x3f;
        }

        public int getSampleDependsOn() {
            return (value >> 4) & 0x03;
        }

        public void setSampleDependsOn(int sdo) {
            value = (sdo & 0x03) << 4 | value & 0xcf;
        }

        public int getSampleIsDependentOn() {
            return (value >> 2) & 0x03;
        }

        public void setSampleIsDependentOn(int sido) {
            value = (sido & 0x03) << 2 | value & 0xf3;
        }

        public int getSampleHasRedundancy() {
            return value & 0x03;
        }

        public void setSampleHasRedundancy(int shr) {
            value = shr & 0x03 | value & 0xfc;
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "isLeading=" + getIsLeading() +
                    ", sampleDependsOn=" + getSampleDependsOn() +
                    ", sampleIsDependentOn=" + getSampleIsDependentOn() +
                    ", sampleHasRedundancy=" + getSampleHasRedundancy() +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Entry entry = (Entry) o;

            if (value != entry.value) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return value;
        }
    }

    public SampleDependencyTypeBox() {
        super(TYPE);
    }

    @Override
    protected long getContentSize() {
        return 4 + entries.size();
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        for (Entry entry : entries) {
            IsoTypeWriter.writeUInt8(byteBuffer, entry.value);
        }
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        while (content.remaining() > 0) {
            entries.add(new Entry(IsoTypeReader.readUInt8(content)));
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
