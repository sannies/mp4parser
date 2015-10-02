package org.mp4parser.boxes.dece;

import org.mp4parser.support.AbstractFullBox;
import org.mp4parser.tools.IsoTypeReader;
import org.mp4parser.tools.IsoTypeWriter;
import org.mp4parser.tools.Utf8;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <pre>
 * aligned(8) class ContentInformationBox
 * extends FullBox(‘cinf’, version=0, flags=0)
 * {
 *  string          mimeSubtypeName;
 *  string          profile-level-idc;
 *  string          codecs;
 *  unsigned int(8) protection;
 *  string          languages;
 *  unsigned int(8) brand_entry_count;
 *  for( int i=0; i &lt; brand_entry_count; i++)
 *  {
 *   string iso_brand;
 *   string version
 *  }
 *  unsigned int(8) id_entry_count;
 *  for( int i=0; i &lt; id_entry_count; i++)
 *  {
 *   string namespace;
 *   string asset_id;
 *  }
 * }
 * </pre>
 */
public class ContentInformationBox extends AbstractFullBox {
    public static final String TYPE = "cinf";

    String mimeSubtypeName;
    String profileLevelIdc;
    String codecs;
    String protection;
    String languages;

    Map<String, String> brandEntries = new LinkedHashMap<String, String>();
    Map<String, String> idEntries = new LinkedHashMap<String, String>();

    public ContentInformationBox() {
        super(TYPE);
    }

    @Override
    protected long getContentSize() {
        long size = 4;
        size += Utf8.utf8StringLengthInBytes(mimeSubtypeName) + 1;
        size += Utf8.utf8StringLengthInBytes(profileLevelIdc) + 1;
        size += Utf8.utf8StringLengthInBytes(codecs) + 1;
        size += Utf8.utf8StringLengthInBytes(protection) + 1;
        size += Utf8.utf8StringLengthInBytes(languages) + 1;
        size += 1;
        for (Map.Entry<String, String> brandEntry : brandEntries.entrySet()) {
            size += Utf8.utf8StringLengthInBytes(brandEntry.getKey()) + 1;
            size += Utf8.utf8StringLengthInBytes(brandEntry.getValue()) + 1;
        }
        size += 1;
        for (Map.Entry<String, String> idEntry : idEntries.entrySet()) {
            size += Utf8.utf8StringLengthInBytes(idEntry.getKey()) + 1;
            size += Utf8.utf8StringLengthInBytes(idEntry.getValue()) + 1;

        }
        return size;
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter.writeZeroTermUtf8String(byteBuffer, mimeSubtypeName);
        IsoTypeWriter.writeZeroTermUtf8String(byteBuffer, profileLevelIdc);
        IsoTypeWriter.writeZeroTermUtf8String(byteBuffer, codecs);
        IsoTypeWriter.writeZeroTermUtf8String(byteBuffer, protection);
        IsoTypeWriter.writeZeroTermUtf8String(byteBuffer, languages);
        IsoTypeWriter.writeUInt8(byteBuffer, brandEntries.size());
        for (Map.Entry<String, String> brandEntry : brandEntries.entrySet()) {
            IsoTypeWriter.writeZeroTermUtf8String(byteBuffer, brandEntry.getKey());
            IsoTypeWriter.writeZeroTermUtf8String(byteBuffer, brandEntry.getValue());
        }
        IsoTypeWriter.writeUInt8(byteBuffer, idEntries.size());
        for (Map.Entry<String, String> idEntry : idEntries.entrySet()) {
            IsoTypeWriter.writeZeroTermUtf8String(byteBuffer, idEntry.getKey());
            IsoTypeWriter.writeZeroTermUtf8String(byteBuffer, idEntry.getValue());

        }
    }

    @Override
    protected void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        mimeSubtypeName = IsoTypeReader.readString(content);
        profileLevelIdc = IsoTypeReader.readString(content);
        codecs = IsoTypeReader.readString(content);
        protection = IsoTypeReader.readString(content);
        languages = IsoTypeReader.readString(content);
        int brandEntryCount = IsoTypeReader.readUInt8(content);
        while (brandEntryCount-- > 0) {
            brandEntries.put(IsoTypeReader.readString(content), IsoTypeReader.readString(content));
        }
        int idEntryCount = IsoTypeReader.readUInt8(content);
        while (idEntryCount-- > 0) {
            idEntries.put(IsoTypeReader.readString(content), IsoTypeReader.readString(content));
        }
    }

    public String getMimeSubtypeName() {
        return mimeSubtypeName;
    }

    public void setMimeSubtypeName(String mimeSubtypeName) {
        this.mimeSubtypeName = mimeSubtypeName;
    }

    public String getProfileLevelIdc() {
        return profileLevelIdc;
    }

    public void setProfileLevelIdc(String profileLevelIdc) {
        this.profileLevelIdc = profileLevelIdc;
    }

    public String getCodecs() {
        return codecs;
    }

    public void setCodecs(String codecs) {
        this.codecs = codecs;
    }

    public String getProtection() {
        return protection;
    }

    public void setProtection(String protection) {
        this.protection = protection;
    }

    public String getLanguages() {
        return languages;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public Map<String, String> getBrandEntries() {
        return brandEntries;
    }

    public void setBrandEntries(Map<String, String> brandEntries) {
        this.brandEntries = brandEntries;
    }

    public Map<String, String> getIdEntries() {
        return idEntries;
    }

    public void setIdEntries(Map<String, String> idEntries) {
        this.idEntries = idEntries;
    }

    public static class BrandEntry {
        String iso_brand;
        String version;

        public BrandEntry(String iso_brand, String version) {
            this.iso_brand = iso_brand;
            this.version = version;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BrandEntry that = (BrandEntry) o;

            if (iso_brand != null ? !iso_brand.equals(that.iso_brand) : that.iso_brand != null) return false;
            if (version != null ? !version.equals(that.version) : that.version != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = iso_brand != null ? iso_brand.hashCode() : 0;
            result = 31 * result + (version != null ? version.hashCode() : 0);
            return result;
        }
    }
}
