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

package org.mp4parser.boxes.dece;

import org.mp4parser.support.AbstractFullBox;
import org.mp4parser.support.DoNotParseDetail;
import org.mp4parser.tools.IsoTypeReader;
import org.mp4parser.tools.Utf8;

import java.nio.ByteBuffer;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * <pre>
 * AssetInformationBox as defined the DECE Common File Format Spec.
 * aligned(8) class AssetInformationBox
 * extends FullBox(‘ainf’, version=1, flags)
 * {
 *  string 				mimeSubtypeName;
 *  string				codecs;
 *  unsigned int(8) 	encrypted;
 *  unsigned int(8) 	entry_count;
 *  for( int i=0; i &lt; entry_count; i++)
 *  {
 *   string	namespace;
 *   string	profile-level-idc;
 *   string	asset_id;
 *  }
 * }
 * </pre>
 */
public class AssetInformationBox extends AbstractFullBox {
    public static final String TYPE = "ainf";

    String apid = "";
    String profileVersion = "0000";

    public AssetInformationBox() {
        super(TYPE);
    }

    @Override
    protected long getContentSize() {
        return Utf8.utf8StringLengthInBytes(apid) + 9;
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        if (getVersion() == 0) {
            byteBuffer.put(Utf8.convert(profileVersion), 0, 4);
            byteBuffer.put(Utf8.convert(apid));
            byteBuffer.put((byte) 0);
        } else {
            throw new RuntimeException("Unknown ainf version " + getVersion());
        }
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        profileVersion = IsoTypeReader.readString(content, 4);
        apid = IsoTypeReader.readString(content);
    }

    public String getApid() {
        return apid;
    }

    public void setApid(String apid) {
        this.apid = apid;
    }

    public String getProfileVersion() {
        return profileVersion;
    }

    public void setProfileVersion(String profileVersion) {
        assert profileVersion != null && profileVersion.length() == 4;
        this.profileVersion = profileVersion;
    }

    @DoNotParseDetail
    public boolean isHidden() {
        return (getFlags() & 1) == 1;
    }

    @DoNotParseDetail
    public void setHidden(boolean hidden) {
        int flags = getFlags();
        if (isHidden() ^ hidden) {
            if (hidden) {
                setFlags(flags | 1);
            } else {
                setFlags(flags & 0xFFFFFE);
            }
        }
    }

    public static class Entry {
        public String namespace;
        public String profileLevelIdc;
        public String assetId;

        public Entry(String namespace, String profileLevelIdc, String assetId) {
            this.namespace = namespace;
            this.profileLevelIdc = profileLevelIdc;
            this.assetId = assetId;
        }

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


}
