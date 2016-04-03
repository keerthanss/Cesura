package com.panoply.cesura;

//import java.io.IOException;
//import java.util.HashMap;

import com.echonest.api.v4.ENItem;
import com.echonest.api.v4.EchoNestAPI;
import com.echonest.api.v4.EchoNestException;
import com.echonest.api.v4.Track;
//import com.echonest.api.v4.util.Commander;

/**
 * Created by eeshwarg on 27-03-2016.
 */
public class TrackAnalysis extends ENItem{
    //Instance variables and class variables
    private EchoNestAPI en;
    private String PATH;
    private String TYPE;
    private TrackAnalysis analysis = null;
    private Track.AnalysisStatus currentStatus = Track.AnalysisStatus.UNKNOWN;

    TrackAnalysis(){
        en = null;
        PATH = null;
        TYPE = null;
    }


    /**
     * Gets the key for the track
     *‣ key: the estimated overall key of a track. The key identifies the tonic triad, the chord, major or minor, which
     represents the final point of rest of a piece
     tonic: the tone on which the music sounds finished
     * @return
     * @throws EchoNestException
     */
    public int getKey() throws EchoNestException {
        fetchBucket("audio_summary");
        return getInteger("audio_summary.key");
    }

    /**
     * Gets the tempo for the track
     *tempo: the overall estimated tempo of a track in beats per minute (BPM). In musical terminology, tempo is the
     speed or pace of a given piece and derives directly from the average beat duration.
     * @return
     * @throws EchoNestException
     */
    public double getTempo() throws EchoNestException {
        fetchBucket("audio_summary");
        return getDouble("audio_summary.tempo");
    }

    /**
     * Gets the mode for the track
     *‣ mode: indicates the modality (major or minor) of a track, the type of scale from which its melodic content is
     derived.
     * @return
     * @throws EchoNestException
     */
    public int getMode() throws EchoNestException {
        fetchBucket("audio_summary");
        return getInteger("audio_summary.mode");
    }

    /**
     * Gets the time signature for the track
     *‣ time signature: an estimated overall time signature of a track. The time signature (meter) is a notational
     convention to specify how many beats are in each bar (or measure).
     * @return
     * @throws EchoNestException
     */
    public int getTimeSignature() throws EchoNestException {
        fetchBucket("audio_summary");
        return getInteger("audio_summary.time_signature");
    }

    /**
     * Gets the duration for the track
     *duration: the duration of a track in seconds as precisely computed by the audio decoder
     * @return
     * @throws EchoNestException
     */
    public double getDuration() throws EchoNestException {
        fetchBucket("audio_summary");
        return getDouble("audio_summary.duration");
    }

    /**
     * Gets the loudness for the track
     *loudness: the overall loudness of a track in decibels (dB). Loudness is the quality of a
     sound that is the primary psychological correlate of physical strength (amplitude).
     * @return
     * @throws EchoNestException

    public double getLoudness() throws EchoNestException {
    fetchBucket("audio_summary");
    return getDouble("audio_summary.loudness");
    }
     */

    /**
     * Gets the energy for the track
     *energy: indicates how energetic a track is
     * @return
     * @throws EchoNestException
     */
    public double getEnergy() throws EchoNestException {
        fetchBucket("audio_summary");
        return getDouble("audio_summary.energy");
    }

    /**
     * Gets the danceability for the track
     *
     * @return
     * @throws EchoNestException
     */
    public double getDanceability() throws EchoNestException {
        fetchBucket("audio_summary");
        return getDouble("audio_summary.danceability");
    }

    /**
     * Gets the speechiness for the track
     *
     * @return
     * @throws EchoNestException
     */
    public double getSpeechiness() throws EchoNestException {
        fetchBucket("audio_summary");
        return getDouble("audio_summary.speechiness");
    }

    /**
     * Gets the liveness for the track
     *
     * @return
     * @throws EchoNestException
     */
    public double getLiveness() throws EchoNestException {
        fetchBucket("audio_summary");
        return getDouble("audio_summary.liveness");
    }



}