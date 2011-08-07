package com.googlecode.mp4parser.authoring;

import com.coremedia.iso.IsoFile;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Transforms a <code>Movie</code> object to an IsoFile. Implementations can
 * determine the specific format: Fragmented MP4, MP4, MP4 with Apple Metadata,
 * MP4 with 3GPP Metadata, MOV.
 */
public interface IsoBuilder {
    /**
     * Builds the actual IsoFile from the Movie.
     * @param movie data source
     * @return the freshly built IsoFile
     */
    public IsoFile build(Movie movie) throws IOException;
}
