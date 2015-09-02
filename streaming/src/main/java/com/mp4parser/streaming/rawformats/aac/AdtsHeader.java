package com.mp4parser.streaming.rawformats.aac;

public class AdtsHeader {
    int getSize() {
        return 7 + (protectionAbsent == 0 ? 2 : 0);
    }

    int sampleFrequencyIndex;

    int mpegVersion;
    int layer;
    int protectionAbsent;
    int profile;
    int sampleRate;

    int channelconfig;
    int original;
    int home;
    int copyrightedStream;
    int copyrightStart;
    int frameLength;
    int bufferFullness;
    int numAacFramesPerAdtsFrame;
}