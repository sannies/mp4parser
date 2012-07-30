package com.googlecode.mp4parser.boxes.mp4.samplegrouping;

import java.nio.ByteBuffer;

/**
* Created with IntelliJ IDEA.
* User: sannies
* Date: 7/30/12
* Time: 3:12 PM
* To change this template use File | Settings | File Templates.
*/
public abstract class GroupEntry {
    public abstract void parse(ByteBuffer byteBuffer);
    public abstract ByteBuffer get();

    public int size() {
        return get().limit();
    }
}
