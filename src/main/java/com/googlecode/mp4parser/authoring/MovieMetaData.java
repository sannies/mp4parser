package com.googlecode.mp4parser.authoring;

/**
 * Contains a movie's metadata independent from the actual boxes containing
 * the data.
 */
public class MovieMetaData {
    private String title;
    private long duration;
    private long timescale;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getTimescale() {
        return timescale;
    }

    public void setTimescale(long timescale) {
        this.timescale = timescale;
    }

    @Override
    public String toString() {
        return "MovieMetaData{" +
                "title='" + title + '\'' +
                ", duration=" + duration +
                ", timescale=" + timescale +
                '}';
    }
}
