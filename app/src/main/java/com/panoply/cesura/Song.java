package com.panoply.cesura;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Song {
    private static final String TAG = "Song";

    public static int RUNNING = 1;
    public static int STOPPED = -1;
    public static int PAUSED = 0;

    private static float percentageToQualifyAsPlay = 0.4f;

    private long id;
    private String title;
    private String artist;
    private int rating;
    private int playCount;
    private long timeSinceLastPlay;
    private String lastPlayTimeStamp;
    private long startPlay, stopPlay, durationPlayed;
    private long duration;
    private int state;

    private String EchoNestID;
    private Context context;

    public Song(Context context, String artist, long id, String title, long duration) {
        this.artist = artist;
        this.id = id;
        this.title = title;
        this.duration = duration;
        this.context = context;

        startPlay = stopPlay = durationPlayed = rating = 0;
        state = STOPPED;
    }

    public Song(Context context, String artist, long id, String title, long duration, String EchoNestId){
        this(context, artist, id, title, duration);
        setEchoNestID(EchoNestId);
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }

    public void setTimeSinceLastPlay(long timeSinceLastPlay) {
        this.timeSinceLastPlay = timeSinceLastPlay;
    }

    public void setEchoNestID(String ID){
        EchoNestID = ID;
        DatabaseOperations databaseOperations = new DatabaseOperations(context);
        databaseOperations.fetchUserProperties(this);
    }

    public String getEchoNestID(){
        return EchoNestID;
    }

    public void markTimeStamp(){
        Log.d(TAG, "marking timestamp for " + title);
        lastPlayTimeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
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

    public int getPlayCount() {
        return playCount;
    }

    public int getRating() {
        return rating;
    }

    public long getLastPlay() {
        String currentTimeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
        DateTime old = new DateTime(lastPlayTimeStamp);
        DateTime current = new DateTime(currentTimeStamp);
        timeSinceLastPlay = current.compareTo(old);
        return timeSinceLastPlay;
    }

    public void startPlaying(){
        Log.d(TAG, "playing " + title);
        startPlay = System.currentTimeMillis();
        durationPlayed = 0;
        state = RUNNING;
    }

    public void pausePlaying(){
        stopPlay = System.currentTimeMillis();
        durationPlayed += stopPlay - startPlay;
        Log.d(TAG, "paused " + title + " with amount played = " + durationPlayed);
        state = PAUSED;
    }

    public void resumePlaying(){
        Log.d(TAG, "resume " + title);
        startPlay = System.currentTimeMillis();
        state = RUNNING;
    }

    public void stopPlaying(){
        stopPlay = System.currentTimeMillis();
        durationPlayed += stopPlay - startPlay;
        Log.d(TAG, "stopped playing " + title + ". Now comparing amount played, " + durationPlayed + " with duration, " + duration);
        if(durationPlayed > (long) (percentageToQualifyAsPlay * duration) ) {
            playCount++;
            markTimeStamp();
            DatabaseOperations databaseOperations = new DatabaseOperations(context);
            databaseOperations.updatePlayCount(EchoNestID, playCount);
        }
        durationPlayed = 0;
        state = STOPPED;
    }

    public int getState(){
        return state;
    }
}

class DateTime{
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int second;

    public DateTime(String date){
        year = Integer.valueOf(date.substring(0, 3));
        month = Integer.valueOf(date.substring(5, 6));
        day = Integer.valueOf(date.substring(8, 9));
        hour = Integer.valueOf(date.substring(11,12));
        minute = Integer.valueOf(date.substring(14,15));
        second = Integer.valueOf(date.substring(17,18));
    }

    public int compareTo(DateTime d){
        int result=0;
        if(year != d.year){
            result = (year - d.year)*24*365;
        }
        else{
            if(month!=d.month){
                result = (month - d.month)*30*24;
            }
            else{
                if(day != d.day){
                    result = (day - d.day)*24;
                }
                else{
                    if(hour != d.hour){
                        result = hour - d.hour;
                    }
                    result = 1;
                }
            }
        }

        return result;
    }
}