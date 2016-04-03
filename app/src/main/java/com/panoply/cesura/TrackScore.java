package com.panoply.cesura;

/**
 * Created by eeshwarg on 03-04-2016.
 */
public class TrackScore {

    private String ID;
    private int key;
    private float tempo;
    private int timeSignature;
    private float loudness;
    private float energy;
    private float danceability;

    public TrackScore(String ID, int key, float tempo, int timeSignature, float loudness, float energy, float danceability, float speechiness) {
        this.ID = ID;
        this.key = key;
        this.tempo = tempo;
        this.timeSignature = timeSignature;
        this.loudness = loudness;
        this.energy = energy;
        this.danceability = danceability;
    }

    public TrackScore() {
        ID = null;
        key = 0;
        tempo = 0;
        timeSignature = 0;
        loudness = 0;
        energy = 0;
        danceability = 0;
    }

    public String getID()
    {
        return ID;
    }

    public void setID(String ID)
    {
        this.ID = ID;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public float getTempo() {
        return tempo;
    }

    public void setTempo(float tempo) {
        this.tempo = tempo;
    }

    public int getTimeSignature() {
        return timeSignature;
    }

    public void setTimeSignature(int timeSignature) {
        this.timeSignature = timeSignature;
    }

    public float getLoudness() {
        return loudness;
    }

    public void setLoudness(float loudness) {
        this.loudness = loudness;
    }

    public float getDanceability() {
        return danceability;
    }

    public void setDanceability(float danceability) {
        this.danceability = danceability;
    }

    public float getEnergy() {
        return energy;
    }

    public void setEnergy(float energy) {
        this.energy = energy;
    }

}


