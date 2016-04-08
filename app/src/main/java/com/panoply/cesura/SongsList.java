package com.panoply.cesura;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.echonest.api.v4.*;
import com.echonest.api.v4.Artist;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Created by eeshwarg on 07-04-2016.
 */
public class SongsList {

    public Context context;
    private static final String TAG = "SongsList";
    private EchoNestAPI en;
    private ArrayList<String> similar_artists;
    private ArrayList<com.echonest.api.v4.Song> songs;
    private ArrayList<TrackScore> songsFromDatabase;

    public SongsList(Context context) throws EchoNestException {
        this.context = context;
        EchoNestAPI en = new EchoNestAPI(context.getString(R.string.EchoNest_API_Key));
        en.setTraceSends(true);
        en.setTraceRecvs(false);
        similar_artists = new ArrayList<>();
        songs = new ArrayList<>();
        songsFromDatabase = new ArrayList<>();
    }

    public void getSimilarArtists() {
        Log.d(TAG, "Getting similar artists");

        try {
            similar_artists = new ArrayList<>();
            EchoNestAPI echoNest = new EchoNestAPI(context.getString(R.string.EchoNest_API_Key));
            echoNest.setTraceSends(true);
            DatabaseOperations db = new DatabaseOperations(context);
            ArrayList<String> artistsFromDatabase = new ArrayList<>();

            Log.d(TAG, "artistsFromDatabase size = 0");
            songsFromDatabase = db.getTopSongs(artistsFromDatabase);
            Log.d(TAG, "artistsFromDatabase size = " + artistsFromDatabase.size());

                for (int i = 0; i < artistsFromDatabase.size(); i++) {
                    List<Artist> artists = echoNest.searchArtists(artistsFromDatabase.get(i));

                    if (artists.size() > 0) {
                        Artist eachArtist = artists.get(0);


                        for (Artist simArtist : eachArtist.getSimilar(2)) {
                            similar_artists.add(simArtist.getName());
                        }
                    }
                }

            for(String art : similar_artists)
                searchSongsByArtist(art, 100);


        } catch (EchoNestException e) {
            Log.e(TAG, "Exception: " + e);
        }
    }

    public void searchSongsByArtist(String artist, int results) throws EchoNestException
    {
        Log.d(TAG, "Getting songs by: " + artist);
        SongParams p = new SongParams();
        p.setArtist(artist);
        p.add("results",results);

        List<com.echonest.api.v4.Song> tracks = en.searchSongs(p);

        Random randomGenerator = new Random(tracks.size());
        int j,i = randomGenerator.nextInt();
        do{
            j = randomGenerator.nextInt(tracks.size());
        }while(j==i);

        songs.add(tracks.get(i));
        songs.add(tracks.get(j));
    }

    public ArrayList<Pair<String, String>> getRecommendations(){
        getSimilarArtists();
        //After calling the above method, the ArrayList 'songs' contains songs of similar artists to top 20 artists.

        ArrayList<com.echonest.api.v4.Song> result = new ArrayList<>();
        Set<String> titles = new HashSet<>();
        for(com.echonest.api.v4.Song song : songs)
            if(titles.add(song.getTitle()))
                result.add(song);
        //The above few lines remove repetitions and create a new list 'result' with distinct 'Song' objects.

        VarianceCalculation obj = new VarianceCalculation(context);
        ArrayList<com.echonest.api.v4.Song> recommendedSongs= obj.calculateVarianceAndSuggestSongs(result, songsFromDatabase);

        ArrayList<Pair<String,String>> ArtistAndTitle = new ArrayList<>();

        for(int i=0;i<recommendedSongs.size();i++)
        {
            String right = songs.get(i).getArtistName();
            String left = songs.get(i).getTitle();
            ArtistAndTitle.get(i).setLeft(left);
            ArtistAndTitle.get(i).setRight(right);
        }

        return ArtistAndTitle;

    }
}
