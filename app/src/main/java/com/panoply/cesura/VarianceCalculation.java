package com.panoply.cesura;

import android.content.Context;
import android.util.Log;

import com.echonest.api.v4.*;
import com.echonest.api.v4.Song;

import java.util.ArrayList;

/**
 * Created by eeshwarg on 03-04-2016.
 */
public class VarianceCalculation {

    public Context context;
    private static final String TAG = "VarianceCalculation";

    public VarianceCalculation(Context context)
    {
        this.context = context;
    }

    public ArrayList<com.echonest.api.v4.Song> calculateVarianceAndSuggestSongs(ArrayList<com.echonest.api.v4.Song> songs, ArrayList<TrackScore> songsFromDatabase) {
        Log.d(TAG,"Calculating variances and suggesting");
        int i, variance[] = new int[songs.size()];
        ArrayList<Song> recSongs = new ArrayList<>();
        for (i = 0; i < songs.size(); i++)
            variance[i] = 0;
        try {
            AttributesOfSong attributes = new AttributesOfSong();
            TrackScore newSong;
            DatabaseOperations db = new DatabaseOperations(context);
            //ArrayList<TrackScore> songsFromDatabase = db.getTopSongs();

            for(i=0;i<songs.size();i++)
            {
                int j,k;
                newSong = attributes.getAttributes(songs.get(i));
                for(j=0;j<20;j++)
                    variance[i] += VarianceOfAllAttributes(newSong, songsFromDatabase.get(j),db);
                int key = variance[i];

                j=0;
                while(key>variance[j] && j<i)
                    j++;
                for(k=j;k<i;k++)
                {
                    variance[k+1] = variance[k];
                    songs.set(k+1,songs.get(k));
                }
                variance[j] = key;
                songs.set(j,songs.get(i));
            }

            int j = 0;
            for(i=0;i<songs.size();i++)
            {
                if(db.isTrackPresent(songs.get(i).getID()))
                        recSongs.add(j,songs.get(i));

            }
        } catch (EchoNestException e) {
            Log.e(TAG,"Exception: " + e);
        }

        return recSongs;
    }

    public int VarianceOfAllAttributes(TrackScore newSong, TrackScore songFromDatabase, DatabaseOperations db)
    {
        Log.d(TAG, "Calculating variance between songs " + songFromDatabase.getID() + " and " + newSong.getID());
        int variance = 0;
        variance += Math.abs(newSong.getDanceability() - songFromDatabase.getDanceability());
        variance += Math.abs((newSong.getKey() - songFromDatabase.getKey()) / 11);
        variance += Math.abs((newSong.getTimeSignature() - songFromDatabase.getTimeSignature()) / 4);
        variance += Math.abs(newSong.getEnergy() - songFromDatabase.getEnergy());
        variance += Math.abs((newSong.getLoudness() - songFromDatabase.getLoudness()) / 200);
        variance += Math.abs((newSong.getTempo() - songFromDatabase.getTempo())/db.getRangeOfTempo());

        return variance;
    }

}
