package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.Utf8;
import com.coremedia.iso.boxes.AbstractFullBox;
import com.coremedia.iso.boxes.Box;

import java.io.IOException;

/**
 * Apple Name box. Allowed as subbox of "----" box.
 *
 * @see AppleGenericBox
 */
public final class AppleNameBox extends AbstractFullBox {
    public static final String TYPE = "name";
    private String name;

    public AppleNameBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    protected long getContentSize() {
        return Utf8.convert(name).length;
    }

    protected void getContent(IsoOutputStream os) throws IOException {
        os.writeStringNoTerm(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        setName(in.readString((int) (size - 4)));
    }
}
