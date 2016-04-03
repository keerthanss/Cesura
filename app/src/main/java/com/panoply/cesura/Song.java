package com.panoply.cesura;

import java.util.ArrayList;

public class Song {
    private long id;
    private String title;
    private String artist;
    private int rating;
    private String genre;
    private int playCount;
    private int timeSinceLastPlay;

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

    public String getGenre() {
        return genre;
    }

    public int getPlayCount() {
        return playCount;
    }

    public int getRating() {
        return rating;
    }

    public int getLastPlay() {
        return timeSinceLastPlay;
    }
}
