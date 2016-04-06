package com.panoply.cesura;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;

public class DatabaseOperations extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "Track Information";

    public static final String COL_ID = "Id";
    public static final String COL_ARTIST = "Artist";
    public static final String COL_GENRE = "Genre";
    public static final String COL_LASTPLAY = "LastPlay";
    public static final String COL_PLAYCOUNT = "PlayCount";
    public static final String COL_KEY = "Key";
    public static final String COL_TEMPO = "Tempo";
    public static final String COL_TIMESIGNATURE = "TimeSignature";
    public static final String COL_LOUDNESS = "Loudness";
    public static final String COL_ENERGY = "Energy";
    public static final String COL_DANCE = "Danceability";
    public static final String COL_SPEECH = "Speechiness";
    public static final String COL_RATING = "Rating";

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
                                + COL_GENRE + " TEXT, "
                                + COL_LASTPLAY + " INTEGER, "
                                + COL_PLAYCOUNT + " INTEGER, "
                                + COL_RATING + " INTEGER, "
                                + COL_KEY + " INTEGER, "
                                + COL_TEMPO + " REAL, "
                                + COL_TIMESIGNATURE + " INTEGER, "
                                + COL_LOUDNESS + " REAL, "
                                + COL_ENERGY + " REAL, "
                                + COL_DANCE + " REAL, "
                                + COL_SPEECH + " REAL);";
        db.execSQL(createSQL);
    }

    public void insertSong(Song song, TrackScore trackScore){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_ID, trackScore.getID());
        contentValues.put(COL_ARTIST, song.getArtist());
        contentValues.put(COL_GENRE, song.getGenre());
        contentValues.put(COL_LASTPLAY, song.getLastPlay());
        contentValues.put(COL_PLAYCOUNT, song.getPlayCount());
        contentValues.put(COL_RATING, song.getRating());
        contentValues.put(COL_KEY, trackScore.getKey());
        contentValues.put(COL_TEMPO, trackScore.getTempo());
        contentValues.put(COL_TIMESIGNATURE, trackScore.getTimeSignature());
        contentValues.put(COL_LOUDNESS, trackScore.getLoudness());
        contentValues.put(COL_ENERGY, trackScore.getEnergy());
        contentValues.put(COL_DANCE, trackScore.getDanceability());
        contentValues.put(COL_SPEECH, trackScore.getSpeechiness());
        db.insert(DB_NAME, null, contentValues);
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
        contentValues.put(COL_SPEECH, trackScore.getSpeechiness());
        db.update(DB_NAME, contentValues, COL_ID + " = ?", new String[]{trackScore.getID()});
    }

    public Cursor getTrackData(String id){
        SQLiteDatabase db = this.getReadableDatabase();
        String findSQL = "SELECT * FROM " + DB_NAME + " WHERE " + COL_ID + " = " + id + "";
        return db.rawQuery(findSQL, null);
    }

    public boolean isTrackPresent(String id){
        Cursor cursor = getTrackData(id);
        if(cursor == null)
            return false;
        cursor.close();
        return true;
    }
}