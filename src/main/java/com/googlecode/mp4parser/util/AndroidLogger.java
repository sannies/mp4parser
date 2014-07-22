package com.googlecode.mp4parser.util;

import android.util.Log;

/**
 * Logs on Android.
 */
public class AndroidLogger extends Logger {
    String name;
    private static final String TAG = "isoparser";

    public AndroidLogger(String name) {
        this.name = name;
    }

    @Override
    public void logDebug(String message) {
        Log.d(TAG, name + ":" + message);
    }

    @Override
    public void logWarn(String message) {
        Log.w(TAG, name + ":" + message);
    }

    @Override
    public void logError(String message) {
        Log.e(TAG, name + ":" + message);
    }
}
