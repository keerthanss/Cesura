package com.panoply.cesura;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Song {
    private long id;
    private String title;
    private String artist;
    private int rating;
    private String genre;
    private int playCount;
    private int timeSinceLastPlay;
    private String lastPlayTimeStamp;

    private class DateTime{
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
                        result = hour - d.hour;
                    }
                }
            }

            return result;
        }
    }

    public Song(String artist, long id, String title) {
        this.artist = artist;
        this.id = id;
        this.title = title;
    }

    public void markTimeStamp(){
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
        String currentTimeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
        DateTime old = new DateTime(lastPlayTimeStamp);
        DateTime current = new DateTime(currentTimeStamp);
        timeSinceLastPlay = current.compareTo(old);
        return timeSinceLastPlay;
    }
}
