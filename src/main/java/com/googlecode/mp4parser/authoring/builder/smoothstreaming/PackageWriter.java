package com.googlecode.mp4parser.authoring.builder.smoothstreaming;

import com.googlecode.mp4parser.authoring.Movie;

import java.io.IOException;

/**
 * Writes the whole package.
 */
public interface PackageWriter {
    public void write(Movie qualities) throws IOException;
}
