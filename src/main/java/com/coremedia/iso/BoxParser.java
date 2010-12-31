package com.coremedia.iso;

import com.coremedia.iso.boxes.BoxInterface;

import java.io.IOException;

/**
 * Basic interface to create boxes from a <code>IsoBufferWrapper</code> and its parent.
 */
public interface BoxParser {
    BoxInterface parseBox(IsoBufferWrapper in, BoxInterface parent, BoxInterface lastMovieFragmentBox) throws IOException;
}
