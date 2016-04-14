package com.panoply.cesura;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;

public class DatabaseOperations extends SQLiteOpenHelper {

    public static final String TAG = "DatabaseOperations";

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "TrackInformation";

    public static final String COL_ID = "Id";
    public static final String COL_ARTIST = "Artist";
    public static final String COL_LASTPLAY = "LastPlay";
    public static final String COL_PLAYCOUNT = "PlayCount";
    public static final String COL_KEY = "Key";
    public static final String COL_TEMPO = "Tempo";
    public static final String COL_TIMESIGNATURE = "TimeSignature";
    public static final String COL_LOUDNESS = "Loudness";
    public static final String COL_ENERGY = "Energy";
    public static final String COL_DANCE = "Danceability";
    public static final String COL_RATING = "Rating";
    public static final String COL_SCORE = "Score";

    public DatabaseOperations(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    /*public DatabaseOperations(Context context, String name, SQLiteDatabase.CursorFactory factory){
        super(context, name, factory, DB_VERSION);
    }*/

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String dropSQL = "DROP TABLE IF EXISTS " + DB_NAME + ";";
        db.execSQL(dropSQL);
        createTable(db);
    }

    private void createTable(SQLiteDatabase db) {
        String createSQL = "CREATE TABLE IF NOT EXISTS " + DB_NAME + " ("
                                + COL_ID + " TEXT PRIMARY KEY, "
                                + COL_ARTIST + " TEXT, "
                                + COL_LASTPLAY + " INTEGER, "
                                + COL_PLAYCOUNT + " INTEGER, "
                                + COL_RATING + " INTEGER, "
                                + COL_KEY + " INTEGER, "
                                + COL_TEMPO + " REAL, "
                                + COL_TIMESIGNATURE + " INTEGER, "
                                + COL_LOUDNESS + " REAL, "
                                + COL_ENERGY + " REAL, "
                                + COL_DANCE + " REAL, "
                                + COL_SCORE + " REAL);";
        db.execSQL(createSQL);
    }

    public void insertSong(localSong song, TrackScore trackScore){
        //if(!isTrackPresent(trackScore.getID())) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(COL_ID, trackScore.getID());
            contentValues.put(COL_ARTIST, song.getArtist());
            contentValues.put(COL_LASTPLAY, song.getLastPlay());
            contentValues.put(COL_PLAYCOUNT, song.getPlayCount());
            contentValues.put(COL_RATING, song.getRating());
            contentValues.put(COL_KEY, trackScore.getKey());
            contentValues.put(COL_TEMPO, trackScore.getTempo());
            contentValues.put(COL_TIMESIGNATURE, trackScore.getTimeSignature());
            contentValues.put(COL_LOUDNESS, trackScore.getLoudness());
            contentValues.put(COL_ENERGY, trackScore.getEnergy());
            contentValues.put(COL_DANCE, trackScore.getDanceability());
            contentValues.put(COL_SCORE, calcScore(song.getPlayCount(), song.getLastPlay(), song.getRating()));
            db.insert(DB_NAME, null, contentValues);
        //}
    }

    private float calcScore(int playCount, long lastPlay, int rating){
        float affinity = playCount;
        affinity /= lastPlay;
        float score = affinity + rating;
        return score;
    }

    public void updateScore(TrackScore trackScore){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_KEY, trackScore.getKey());
        contentValues.put(COL_TEMPO, trackScore.getTempo());
        contentValues.put(COL_TIMESIGNATURE, trackScore.getTimeSignature());
        contentValues.put(COL_LOUDNESS, trackScore.getLoudness());
        contentValues.put(COL_ENERGY, trackScore.getEnergy());
        contentValues.put(COL_DANCE, trackScore.getDanceability());
        db.update(DB_NAME, contentValues, COL_ID + " = ?", new String[]{trackScore.getID()});
    }

    public void updateRating(String id, int rating){
        Log.d(TAG, "Updating rating of " + id + " to " + rating);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_RATING, rating);
        db.update(DB_NAME, contentValues, COL_ID + " = ?", new String[]{id});
    }

    public void updatePlayCount(String id, int playCount){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_PLAYCOUNT, playCount);
        db.update(DB_NAME, contentValues, COL_ID + " = ?", new String[]{id});
    }

    public boolean fetchUserProperties(localSong song){
        String id = song.getEchoNestID();
        Cursor cursor = getTrackData(id);
        boolean result = false;
        if (cursor != null && cursor.moveToFirst() ){
            Log.d(TAG, "Fetching user properties " + cursor.toString());
            song.setRating(cursor.getInt(cursor.getColumnIndex(COL_RATING)));
            song.setTimeSinceLastPlay(cursor.getLong(cursor.getColumnIndex(COL_RATING)));
            song.setPlayCount(cursor.getInt(cursor.getColumnIndex(COL_RATING)));
            result = true;
            cursor.close();
        }
        return result;
    }

    public Cursor getTrackData(String id){
        Log.d(TAG, "Fetching track data for " + id);
        SQLiteDatabase db = this.getReadableDatabase();
        String findSQL = "SELECT * FROM " + DB_NAME + " WHERE " + COL_ID + " = \"" + id + "\";";
        return db.rawQuery(findSQL, null);
    }

    public float getRangeOfTempo(){
        SQLiteDatabase db = this.getReadableDatabase();
        float maxTempo = 100.0f, minTempo = 0.0f;
        float range;
        String selectMax = "SELECT MAX(" + COL_TEMPO + ") FROM " + DB_NAME;
        String selectMin = "SELECT MIN(" + COL_TEMPO + ") FROM " + DB_NAME;
        Cursor cursorMax = db.rawQuery(selectMax, null);
        Cursor cursorMin = db.rawQuery(selectMin, null);
        if(cursorMax != null){
            cursorMax.moveToFirst();
            maxTempo = cursorMax.getFloat(0);
            cursorMax.close();
        }
        if(cursorMin!=null){
            cursorMin.moveToFirst();
            minTempo = cursorMin.getFloat(0);
            cursorMin.close();
        }
        range = maxTempo - minTempo;
        return range;
    }

    public ArrayList<TrackScore> getTopSongs(ArrayList<String> artists){
        SQLiteDatabase db = this.getReadableDatabase();
        String topQuerySQL = "SELECT " + COL_ID
                                + ", " + COL_KEY
                                + ", " + COL_TEMPO
                                + ", " + COL_TIMESIGNATURE
                                + ", " + COL_LOUDNESS
                                + ", " + COL_ENERGY
                                + ", " + COL_DANCE
                                + ", " + COL_ARTIST
                                + " FROM " + DB_NAME
                                + " ORDER BY " + COL_SCORE
                                + " DESC"
                                + " LIMIT 20";
        Cursor cursor = db.rawQuery(topQuerySQL, null);
        ArrayList<TrackScore> trackScores = new ArrayList<>();
        if(cursor!=null && cursor.moveToFirst()){
            while(!cursor.isAfterLast()) {
                TrackScore score = new TrackScore();

                score.setID(cursor.getString(cursor.getColumnIndex(COL_ID)));
                score.setKey(cursor.getInt(cursor.getColumnIndex(COL_KEY)));
                score.setTempo(cursor.getFloat(cursor.getColumnIndex(COL_TEMPO)));
                score.setTimeSignature(cursor.getInt(cursor.getColumnIndex(COL_TIMESIGNATURE)));
                score.setLoudness(cursor.getFloat(cursor.getColumnIndex(COL_LOUDNESS)));
                score.setEnergy(cursor.getFloat(cursor.getColumnIndex(COL_ENERGY)));
                score.setDanceability(cursor.getFloat(cursor.getColumnIndex(COL_DANCE)));

                artists.add(cursor.getString(cursor.getColumnIndex(COL_ARTIST)));

                trackScores.add(score);

                cursor.moveToNext();
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return trackScores;
    }

    public boolean isTrackPresent(String id){
        Cursor cursor = getTrackData(id);
        if(cursor == null)
            return false;
        cursor.close();
        return true;
    }

}
