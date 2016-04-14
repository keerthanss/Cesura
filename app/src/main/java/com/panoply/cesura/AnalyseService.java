package com.panoply.cesura;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.echonest.api.v4.EchoNestAPI;
import com.echonest.api.v4.EchoNestException;
import com.echonest.api.v4.Song;
import com.echonest.api.v4.SongParams;

import java.util.ArrayList;
import java.util.List;

public class AnalyseService extends Service {

    public static final String TAG = "AnalyseService";

    private ArrayList<localSong> localSongs;
    private DatabaseOperations databaseOperations;
    private EchoNestAPI echoNestAPI;

    private final IBinder binder = new AnalyseBinder();

    public class AnalyseBinder extends Binder {
        AnalyseService getService(){
            return AnalyseService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        databaseOperations = new DatabaseOperations(this);
        localSongs = new ArrayList<>();
        echoNestAPI = new EchoNestAPI(getString(R.string.EchoNest_API_Key));
    }

    public void setLocalSongs(ArrayList<localSong> localSongs){
        this.localSongs = localSongs;
    }

    public void analyseAll(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                for(localSong song : localSongs){
                    try {
                        analyseSong(song);
                    } catch (EchoNestException e) {
                        Log.e(TAG, "Request limit reached. Sleeping...");
                        try {
                            Thread.sleep(60000, 0);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }

        });
        thread.start();
    }

    private void analyseSong(localSong song) throws EchoNestException{
        if(!checkIfPresent(song)) {
            TrackScore trackScore = new AttributesOfSong(this).getAttributes(new Pair<String, String>(song.getTitle(), song.getArtist()));
            databaseOperations.insertSong(song, trackScore);
            song.setEchoNestID(trackScore.getID());
        }
        Log.d(TAG, "After analysis, " + song.toString());

    }

    private boolean checkIfPresent(localSong song){
        SongParams params = new SongParams();
        params.setTitle(song.getTitle());
        params.setArtist(song.getArtist());
        boolean result = false;
        try {
            List<Song> list = echoNestAPI.searchSongs(params);
            if ( !(list == null || list.size() == 0) ) {
                String id = list.get(0).getID();
                result = databaseOperations.isTrackPresent(id);
                if(result)
                    song.setEchoNestID(id);
            }

        } catch (EchoNestException e){
            Log.e(TAG, "Couldn't check if present");
            result = false;
        }
        Log.d(TAG, "Is " + song.getTitle() + " present already? " + result);
        return result;
    }

    public void updateUserCharacteristics(){
        for(localSong song: localSongs){
            databaseOperations.fetchUserProperties(song);
        }
    }
}
