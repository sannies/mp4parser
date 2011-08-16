package com.googlecode.mp4parser.authoring;

/**
 * Contains a movie's metadata independent from the actual boxes containing
 * the data.
 */
public class MovieMetaData {
    private String title;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    @Override
    public String toString() {
        return "MovieMetaData{" +
                "title='" + title + '\'' + '}';
    }
}
