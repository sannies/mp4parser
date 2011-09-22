package com.googlecode.mp4parser.h264;


import java.io.IOException;

public interface AccessUnitSource {
    AccessUnit nextAccessUnit() throws IOException;
}
