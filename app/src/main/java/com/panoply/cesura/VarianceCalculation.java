package com.panoply.cesura;

import android.content.Context;

import com.echonest.api.v4.EchoNestException;

import java.util.ArrayList;

/**
 * Created by eeshwarg on 03-04-2016.
 */
public class VarianceCalculation {

    public Context context;

    public VarianceCalculation(Context context)
    {
        this.context = context;
    }

    public ArrayList<Song> calculateVariance(ArrayList<Song> songs) {
        int i, variance[] = new int[songs.size()];
        for (i = 0; i < songs.size(); i++)
            variance[i] = 0;
        try {
            AttributesOfSong attributes = new AttributesOfSong();
            TrackScore newSong;
            DatabaseOperations db = new DatabaseOperations(context);
            ArrayList<TrackScore> songsFromDatabase = db.getTopSongs();

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

        } catch (EchoNestException e) {
            System.out.println("Exception: " + e);
        }

        return songs;
    }

    public int VarianceOfAllAttributes(TrackScore newSong, TrackScore songFromDatabase, DatabaseOperations db)
    {
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
