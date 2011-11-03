package com.googlecode.mp4parser.boxes.ultraviolet;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.AbstractFullBox;
import com.coremedia.iso.boxes.Box;

import java.io.IOException;

/**
 * AssetInformationBox as defined Common File Format Spec.
 */
public class AssetInformationBox extends AbstractFullBox {
    String apid;
    String profileVersion;

    public AssetInformationBox() {
        super(IsoFile.fourCCtoBytes("ainf"));
    }

    @Override
    protected long getContentSize() {
        return IsoFile.utf8StringLengthInBytes(apid) + 1 + 4;
    }

    @Override
    protected void getContent(IsoOutputStream os) throws IOException {
        os.write(IsoFile.fourCCtoBytes(profileVersion));
        os.writeStringZeroTerm(apid);
    }

    @Override
    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        profileVersion = in.readString(4);
        apid = in.readString();
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
}
