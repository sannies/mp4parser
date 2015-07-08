package com.mp4parser.streaming;


import com.coremedia.iso.BoxParser;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.DataSource;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class WriteOnlyBox implements Box {
    private final String type;
    private Container parent;

    public Container getParent() {
        return parent;
    }

    public void setParent(Container parent) {
        this.parent = parent;
    }

    public long getOffset() {
        throw new RuntimeException("It's a´write only box");
    }

    public void parse(DataSource dataSource, ByteBuffer header, long contentSize, BoxParser boxParser) throws IOException {
        throw new RuntimeException("It's a´write only box");
    }

    public WriteOnlyBox(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
