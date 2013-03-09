package com.googlecode.mp4parser.util;

import java.util.logging.Level;

public class JuliLogger extends Logger {
    java.util.logging.Logger logger;

    public JuliLogger(String name) {
        logger = java.util.logging.Logger.getLogger(name);
    }

    @Override
    public void logDebug(String message) {
        logger.log(Level.FINE, message);
    }

    @Override
    public void logWarn(String message) {
        logger.log(Level.WARNING, message);
    }

    @Override
    public void logError(String message) {
        logger.log(Level.SEVERE, message);
    }
}
