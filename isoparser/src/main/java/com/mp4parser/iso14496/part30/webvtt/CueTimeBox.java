package com.mp4parser.iso14496.part30.webvtt;

import com.mp4parser.streaming.WriteOnlyBox;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;

/**
 * Created by sannies on 11.08.2015.
 */
public class CueTimeBox extends AbstractCueBox {

    public CueTimeBox() {
        super("ctim");
    }

}
