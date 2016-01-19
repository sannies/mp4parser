package org.mp4parser.support;

public abstract class Logger {

    public static Logger getLogger(Class clz) {

        if (System.getProperty("java.vm.name").equalsIgnoreCase("Dalvik")) {
            return new AndroidLogger(clz.getSimpleName());
        } else {
            return new JuliLogger(clz.getSimpleName());
        }
    }

    public abstract void logDebug(String message);

    public abstract void logWarn(String message);

    public abstract void logError(String message);
}
