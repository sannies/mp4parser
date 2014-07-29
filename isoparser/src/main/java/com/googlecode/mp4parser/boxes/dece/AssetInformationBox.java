/*
 * Copyright 2011 castLabs, Berlin
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

package com.googlecode.mp4parser.boxes.dece;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.coremedia.iso.Utf8;
import com.googlecode.mp4parser.AbstractFullBox;
import com.googlecode.mp4parser.annotations.DoNotParseDetail;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * AssetInformationBox as defined the DECE Common File Format Spec.
 * aligned(8) class AssetInformationBox
 * extends FullBox(‘ainf’, version=1, flags)
 * {
 * string 				mime_subtype_name;
 * string				codecs;
 * unsigned int(8) 	encrypted;
 * unsigned int(8) 	entry_count;
 * for( int i=0; i < entry_count; i++)
 * {
 * string	namespace;
 * string	profile-level-idc;
 * string	asset_id;
 * }
 * }
 */
public class AssetInformationBox extends AbstractFullBox {
    public static final String TYPE = "ainf";

    String v0Apid = "";
    String v0ProfileVersion = "0000";

    String v1MimeSubtypeName;
    String v1Codecs;
    boolean v1Encrypted;

    List<Entry> v1_Entries = new ArrayList<Entry>();

    public static class Entry {
        public Entry(String namespace, String profileLevelIdc, String assetId) {
            this.namespace = namespace;
            this.profileLevelIdc = profileLevelIdc;
            this.assetId = assetId;
        }

        public String namespace;
        public String profileLevelIdc;
        public String assetId;

        @Override
        public String toString() {
            return "{" +
                    "namespace='" + namespace + '\'' +
                    ", profileLevelIdc='" + profileLevelIdc + '\'' +
                    ", assetId='" + assetId + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Entry entry = (Entry) o;

            if (!assetId.equals(entry.assetId)) return false;
            if (!namespace.equals(entry.namespace)) return false;
            if (!profileLevelIdc.equals(entry.profileLevelIdc)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = namespace.hashCode();
            result = 31 * result + profileLevelIdc.hashCode();
            result = 31 * result + assetId.hashCode();
            return result;
        }

        public int getSize() {
            return 3 + Utf8.utf8StringLengthInBytes(namespace) +
                    Utf8.utf8StringLengthInBytes(profileLevelIdc) + Utf8.utf8StringLengthInBytes(assetId);
        }
    }

    public AssetInformationBox() {
        super(TYPE);
    }

    @Override
    protected long getContentSize() {
        if (getVersion() ==0 ) {
            return Utf8.utf8StringLengthInBytes(v0Apid) + 9;
        } else if (getVersion() ==1 ) {
            long size =
                    4 +
                    Utf8.utf8StringLengthInBytes(v1MimeSubtypeName) + 1 +
                    Utf8.utf8StringLengthInBytes(v1Codecs) + 1 +
                    2;
            for (Entry v1_entry : v1_Entries) {
                size += v1_entry.getSize();
            }
            return size;

        } else {
            throw new RuntimeException("Unknown ainf version " + getVersion());
        }
    }


    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        if (getVersion() == 0) {
            byteBuffer.put(Utf8.convert(v0ProfileVersion), 0, 4);
            byteBuffer.put(Utf8.convert(v0Apid));
            byteBuffer.put((byte) 0);
        } else  if (getVersion() == 1) {
            IsoTypeWriter.writeUtf8String(byteBuffer, v1MimeSubtypeName);
            IsoTypeWriter.writeUtf8String(byteBuffer, v1Codecs);
            IsoTypeWriter.writeUInt8(byteBuffer, v1Encrypted ?1:0);
            IsoTypeWriter.writeUInt8(byteBuffer, v1_Entries.size());
            for (Entry v1_entry : v1_Entries) {
                IsoTypeWriter.writeUtf8String(byteBuffer, v1_entry.namespace);
                IsoTypeWriter.writeUtf8String(byteBuffer, v1_entry.profileLevelIdc);
                IsoTypeWriter.writeUtf8String(byteBuffer, v1_entry.assetId );
            }
        } else {
            throw new RuntimeException("Unknown ainf version " + getVersion());
        }
    }


    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        if (getVersion() == 0) {
            v0ProfileVersion = IsoTypeReader.readString(content, 4);
            v0Apid = IsoTypeReader.readString(content);
            content = null;

        } else if (getVersion() == 1) {
            v1MimeSubtypeName = IsoTypeReader.readString(content);
            v1Codecs = IsoTypeReader.readString(content);
            v1Encrypted = IsoTypeReader.readUInt8(content)==1;
            int i = IsoTypeReader.readUInt8(content);
            while (i-- > 0) {
                v1_Entries.add(
                        new Entry(
                                IsoTypeReader.readString(content),
                                IsoTypeReader.readString(content),
                                IsoTypeReader.readString(content)));
            }
        } else {
            throw new RuntimeException("Unknown ainf version " + getVersion());
        }
    }

    public String getV0Apid() {
        return v0Apid;
    }

    public void setV0Apid(String v0_apid) {
        this.v0Apid = v0_apid;
    }

    public String getV0ProfileVersion() {
        return v0ProfileVersion;
    }

    public void setV0ProfileVersion(String v0_profileVersion) {
        assert v0_profileVersion != null && v0_profileVersion.length() == 4;
        this.v0ProfileVersion = v0_profileVersion;
    }

    public String getV1MimeSubtypeName() {
        return v1MimeSubtypeName;
    }

    public void setV1MimeSubtypeName(String v1_MimeSubtypeName) {
        this.v1MimeSubtypeName = v1_MimeSubtypeName;
    }

    public String getV1Codecs() {
        return v1Codecs;
    }

    public void setV1Codecs(String v1_Codecs) {
        this.v1Codecs = v1_Codecs;
    }

    public boolean isV1Encrypted() {
        return v1Encrypted;
    }

    public void setV1Encrypted(boolean v1_Encrypted) {
        this.v1Encrypted = v1_Encrypted;
    }

    public List<Entry> getV1Entries() {
        return v1_Entries;
    }

    public void setV1Entries(List<Entry> v1_Entries) {
        this.v1_Entries = v1_Entries;
    }



    @DoNotParseDetail
    public boolean isV1Hidden() {
        return (getFlags() & 1) == 1;
    }

    @DoNotParseDetail
    public void setV1Hidden(boolean hidden) {
        int flags = getFlags();
        if (isV1Hidden() ^ hidden) {
            if (hidden) {
                setFlags(flags | 1);
            } else {
                setFlags(flags & 0xFFFFFE);
            }
        }
    }


}
