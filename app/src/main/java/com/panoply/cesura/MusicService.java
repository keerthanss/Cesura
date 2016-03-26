package com.panoply.cesura;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private static final String TAG = "MusicService";
    private static final int NOTIFY_ID = 1;

    private MediaPlayer mediaPlayer;
    private int songPosition;
    private ArrayList<Song> songArrayList;
    private final IBinder binder = new MusicBinder();
    private String songTitle = "";

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        initMusicPlayer();
        songPosition = 0;
    }

    private void initMusicPlayer(){
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
    }

    public void setList(ArrayList<Song> songArrayList){
        Log.d(TAG, "the song list has been set");
        this.songArrayList = songArrayList;
    }

    public void setSongPosition(int songPosition){
        Log.d(TAG, "song position chosen = " + songPosition);
        this.songPosition = songPosition;
    }

    public class MusicBinder extends Binder {
        MusicService getService(){
            return MusicService.this;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "media player prepared");
        mp.start();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.android_music_player_play)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle);
        Notification notif = builder.build();
        startForeground(NOTIFY_ID, notif);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mediaPlayer.stop();
        mediaPlayer.release();
        return false;
    }

    public void playSong(){
        Log.d(TAG, "playing song");
        mediaPlayer.reset();
        Song song = songArrayList.get(songPosition);
        songTitle = song.getTitle();
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.getId());
        try {
            mediaPlayer.setDataSource(getApplicationContext(), trackUri);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error setting data source :" + e );
        }
        mediaPlayer.prepareAsync();
    }

    public int getCurrentPosition(){
        return mediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }

    public void pausePlayer() {
        mediaPlayer.pause();
    }

    public void seek(int position){
        mediaPlayer.seekTo(position);
    }

    public void startPlayer(){
        mediaPlayer.start();
    }


    public void playNext(){
        songPosition++;
        if(songPosition >= songArrayList.size())
            songPosition = 0;
        playSong();
    }

    public void playPrev(){
        songPosition--;
        if(songPosition < 0 )
            songPosition = songArrayList.size() - 1;
        playSong();
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }
}

