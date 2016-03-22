package com.panoply.cesura;

import java.util.ArrayList;

public class Song {
    private long id;
    private String title;
    private String artist;

    public Song(String artist, long id, String title) {
        this.artist = artist;
        this.id = id;
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
