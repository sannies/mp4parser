package org.mp4parser.streaming.extensions;

import org.mp4parser.streaming.TrackExtension;

/**
 * Created by sannies on 17.08.2015.
 */
public class DimensionTrackExtension implements TrackExtension {
    int width;
    int height;

    public DimensionTrackExtension(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return "width=" + width + ", height=" + height;
    }
}
