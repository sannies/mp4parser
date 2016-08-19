package org.mp4parser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The classic version object.
 */
public class Version {
    public static final String VERSION;
    private static final Logger LOG = LoggerFactory.getLogger(Version.class);

    static {
        LineNumberReader lnr = new LineNumberReader(new InputStreamReader(Version.class.getResourceAsStream("/version.txt")));
        String version;
        try {
            version = lnr.readLine();
        } catch (IOException e) {
            LOG.warn(e.getMessage());
            version = "unknown";
        }
        VERSION = version;

    }
}
