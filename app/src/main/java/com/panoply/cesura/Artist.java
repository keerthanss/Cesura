package com.panoply.cesura;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Artist {
    private String name;
    private ArrayList<Song> songsByArtist;

    public Artist(String name, ArrayList<Song> songsByArtist) {
        this.name = name;
        this.songsByArtist = songsByArtist;
    }

    public String getName(){ return name;}

    public ArrayList<Song> getSongsByArtist(){ return songsByArtist;}

    public int getNumberOfSongs(){ return songsByArtist.size();}

    private void addSong(Song song){
        songsByArtist.add(song);
    }

    public static ArrayList<Artist> fetchArtists(ArrayList<Song> allSongs){
        ArrayList<Artist> allArtists = new ArrayList<>();
        Collections.sort(allSongs, new Comparator<Song>() {
            @Override
            public int compare(Song lhs, Song rhs) {
                return lhs.getArtist().compareTo(rhs.getArtist());
            }
        });
        for(int i = 0; i<allSongs.size();){
            String artistName = allSongs.get(i).getArtist();
            Artist artist = new Artist(artistName, new ArrayList<Song>());
            int j = i;
            while(j < allSongs.size() && allSongs.get(j).getArtist().equals(artistName)){
                artist.addSong(allSongs.get(j));
                j++;
            }
            i=j;
            allArtists.add(artist);
        }
        return allArtists;
    }
}
