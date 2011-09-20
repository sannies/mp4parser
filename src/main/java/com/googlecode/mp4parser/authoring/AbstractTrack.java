package com.googlecode.mp4parser.authoring;

/**
 *
 */
public abstract class AbstractTrack implements Track {
    private boolean enabled = true;
    private boolean inMovie = true;
    private boolean inPreview = true;
    private boolean inPoster = true;

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isInMovie() {
        return inMovie;
    }

    public boolean isInPreview() {
        return inPreview;
    }

    public boolean isInPoster() {
        return inPoster;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setInMovie(boolean inMovie) {
        this.inMovie = inMovie;
    }

    public void setInPreview(boolean inPreview) {
        this.inPreview = inPreview;
    }

    public void setInPoster(boolean inPoster) {
        this.inPoster = inPoster;
    }

}
