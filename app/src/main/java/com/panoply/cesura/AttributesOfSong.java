package com.panoply.cesura;

/**
 * Created by eeshwarg on 03-04-2016.
 */
import com.echonest.api.v4.EchoNestAPI;
import com.echonest.api.v4.EchoNestException;
import com.echonest.api.v4.Song;
import com.echonest.api.v4.SongParams;
import java.util.List;


public class AttributesOfSong{

    private EchoNestAPI en;

    public AttributesOfSong() throws EchoNestException {
        en = new EchoNestAPI();
        en.setTraceSends(true);
        en.setTraceRecvs(false);
    }

    public TrackScore getAttributes(com.panoply.cesura.Song song)
            throws EchoNestException {
        SongParams p = new SongParams();
        TrackScore track = new TrackScore();
        p.setArtist(song.getArtist());
        p.setTitle(song.getTitle());
        p.setResults(1);
        p.includeAudioSummary();
        List<Song> songs = en.searchSongs(p);
        if (songs.size() > 0) {
            int key = songs.get(0).getKey();
            track.setKey(key);

            float tempo = (float)songs.get(0).getTempo();
            track.setTempo(tempo);

            int timeSignature = songs.get(0).getTimeSignature();
            track.setTimeSignature(timeSignature);

            float loudness = (float)songs.get(0).getLoudness();
            track.setLoudness(loudness);

            float energy = (float)songs.get(0).getEnergy();
            track.setEnergy(energy);

            float danceability = (float)songs.get(0).getDanceability();
            track.setDanceability(danceability);

            return track;
        } else {
            return null;
        }
    }

    /*public static void main(String[] args) throws EchoNestException {
        AttributesOfSong song = new AttributesOfSong();
        TrackScore track;
        System.out.println("Enter artist and song name: ");

        String artist=null, title=null;

        //Still have to get artist name and title

        track = song.getAttributes(artist,title);
        if (track != null) {
            System.out.println("Key: " + track.getKey());
            System.out.println("Tempo: " + track.getTempo());
            System.out.println("Time signature: " + track.getTimeSignature());
            System.out.println("Loudness: " + track.getLoudness());
            System.out.println("Energy: " + track.getEnergy());
            System.out.println("Danceability: " + track.getDanceability());
        }
        else
            System.out.println("Couldn't retrieve data");
    }*/

}
