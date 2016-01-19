package org.mp4parser.streaming.input.aac;

public class AdtsHeader {
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

    int getSize() {
        return 7 + (protectionAbsent == 0 ? 2 : 0);
    }
}