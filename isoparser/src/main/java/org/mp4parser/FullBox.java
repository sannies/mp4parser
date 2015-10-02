package org.mp4parser;

/**
 * The <code>FullBox</code> contains all getters and setters specific
 * to a so-called full box according to the ISO/IEC 14496/12 specification.
 */
public interface FullBox extends ParsableBox {
    int getVersion();

    void setVersion(int version);

    int getFlags();

    void setFlags(int flags);
}
