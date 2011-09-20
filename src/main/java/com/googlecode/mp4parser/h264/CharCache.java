package com.googlecode.mp4parser.h264;

public class CharCache {
    private char[] cache;
    private int pos;

    public CharCache(int capacity) {
        cache = new char[capacity];
    }

    public void append(String str) {
        char[] chars = str.toCharArray();
        int available = cache.length - pos;
        int toWrite = chars.length < available ? chars.length : available;
        System.arraycopy(chars, 0, cache, pos, toWrite);
        pos += toWrite;
    }

    public String toString() {
        return new String(cache, 0, pos);
    }

    public void clear() {
        pos = 0;
    }

    public void append(char c) {
        if (pos < cache.length - 1) {
            cache[pos] = c;
            pos++;
        }
    }

    public int length() {
        return pos;
    }
}
