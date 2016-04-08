package com.panoply.cesura;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.echonest.api.v4.EchoNestException;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MediaController.MediaPlayerControl {

    private static final String TAG = "MainActivity";

    public static final String TRANSMIT_TITLE = "songTitleInfo";
    public static final String TRANSMIT_ARTIST = "songArtistInfo";

    private ListView songListView;
    private ArrayList<localSong> songArrayList;

    private ArrayList<localSong> playingQueue;
    private ArrayList<Artist> artistArrayList;

    private SongAdapter adapter;

    private MusicService musicService;
    private Intent playIntent;
    private boolean musicBound = false;

    private MusicController controller;

    private SongDataReceiver receiver;

    private SongsList songRecs;

    private DatabaseOperations databaseOperations;

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Service connected");
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            musicService.setList(songArrayList);
            musicService.setPlayingQueue(playingQueue);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        songArrayList = new ArrayList<localSong>();
        populateSongList();
        artistArrayList = Artist.fetchArtists(songArrayList);
        Collections.sort(songArrayList, new Comparator<localSong>() {
            @Override
            public int compare(localSong lhs, localSong rhs) {
                return lhs.getTitle().compareTo(rhs.getTitle());
            }
        });
        playingQueue = songArrayList;

        songListView = (ListView)findViewById(R.id.songList);
        adapter = new SongAdapter(this, songArrayList, artistArrayList);
        adapter.changeMode(SongAdapter.SHOW_SONGS);
        adapter.setPlayingQueue(playingQueue);
        songListView.setAdapter(adapter);

        LinearLayout root = (LinearLayout) findViewById(R.id.linLayout);
        ViewTreeObserver vto = root.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                setController();
            }
        });

        try {
            songRecs = new SongsList(this);
        }catch (EchoNestException e){
            Log.e(TAG, "Error : " + e);
        }

        databaseOperations = new DatabaseOperations(this);
    }

    private void setController(){
        Log.d(TAG, "Setting up the MusicController");
        controller = new MusicController(this);
        controller.setPrevNextListeners(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playNext();
                    }
                },
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playPrev();
                    }
                }
        );
        controller.setMediaPlayer(this);
        //try {
        //    controller.setAnchorView(findViewById(R.id.linLayout), musicService.getCurrentSong().getTitle(), musicService.getCurrentSong().getArtist());
        //} catch (NullPointerException e){
            controller.setAnchorView(findViewById(R.id.linLayout));
        //    Log.d(TAG, "music service not connected yet");
        //}
        controller.setEnabled(true);
        controller.show();
        setupReceiver();
    }

    private void setupReceiver(){
        if(receiver != null)
            unregisterReceiver(receiver);
        receiver = new SongDataReceiver(controller, findViewById(R.id.linLayout));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicService.TRANSMIT_ACTION);
        registerReceiver(receiver, intentFilter);
    }

    private void playNext(){
        musicService.playNext();
        //setController();
        controller.show(0);
    }

    private void playPrev(){
        musicService.playPrev();
        //setController();
        controller.show(0);
    }

    public void populateSongList(){
        Log.d(TAG, "Populating the list of songs");
        ContentResolver musicResolver = getContentResolver();
        Uri externalMusicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        /*
        Uri internalMusicUri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
        Support for internal audio required?
         */
        Cursor musicCursor = musicResolver.query(externalMusicUri, null, null, null, null);

        if(musicCursor!=null && musicCursor.moveToFirst()){
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int durationColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

            localSong song;
            do{
                song = new localSong(this,
                                musicCursor.getString(artistColumn), musicCursor.getLong(idColumn),
                                musicCursor.getString(titleColumn), musicCursor.getLong(durationColumn));
                songArrayList.add(song);
                try {
                    TrackScore trackScore = new AttributesOfSong().getAttributes(new Pair<String,String>(song.getTitle(),song.getArtist()));
                    databaseOperations.insertSong(song, trackScore);
                }catch(EchoNestException e){
                    Log.e(TAG, "Error fetching attributes");
                }
            } while(musicCursor.moveToNext());
        }
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
        if(playIntent == null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent,musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch(id){
            case R.id.nav_songs:
                adapter.changeMode(SongAdapter.SHOW_SONGS);
                songListView.setAdapter(adapter);
                break;
            case R.id.nav_artists:
                adapter.changeMode(SongAdapter.SHOW_ARTISTS);
                songListView.setAdapter(adapter);
                break;
            /*case R.id.nav_playlists:
                break;*/
            case R.id.nav_nowplaying:
                adapter.changeMode(SongAdapter.SHOW_QUEUE);
                songListView.setAdapter(adapter);
                break;
            case R.id.nav_recommendation:
                adapter.setRecommendations(songRecs.getRecommendations());
                adapter.changeMode(SongAdapter.SHOW_RECOMMENDATIONS);
                songListView.setAdapter(adapter);
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void songPicked(View view){
        switch (adapter.getMode()){
            case SongAdapter.SHOW_SONGS:
                playingQueue = songArrayList;
                break;
            case SongAdapter.SHOW_QUEUE:
                break;
        }
        musicService.setPlayingQueue(playingQueue);
        adapter.setPlayingQueue(playingQueue);
        musicService.setSongPosition(Integer.parseInt(view.getTag().toString()));
        musicService.playSong();
        //setController();
    }

    public void artistPicked(View view){
        Artist artist = artistArrayList.get(Integer.parseInt(view.getTag().toString()));
        playingQueue = artist.getSongsByArtist();
        musicService.setPlayingQueue(playingQueue);
        adapter.setPlayingQueue(playingQueue);
        musicService.setSongPosition(0);
        musicService.playSong();
        //setController();
    }

    @Override
    public void start() {
        //if(musicService!=null && musicBound)
        Log.d(TAG, "Starting MediaPlayer");
        musicService.startPlayer();
        //setController();
    }

    @Override
    public void pause() {
        Log.d(TAG,"Pause");
        musicService.pausePlayer();
    }

    @Override
    public int getDuration() {
        if(musicService!=null && musicBound && musicService.isPlaying())
            return musicService.getDuration();
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(musicService!=null && musicBound && musicService.isPlaying())
            return musicService.getCurrentPosition();
        return 0;
    }

    @Override
    public void seekTo(int pos) {
        if(musicService!=null && musicBound)
            musicService.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if(musicService!=null && musicBound)
            return musicService.isPlaying();
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}












class SongAdapter extends BaseAdapter{

    public static final int SHOW_SONGS = 0;
    public static final int SHOW_ARTISTS = 1;
    public static final int SHOW_PLAYLISTS = 2;
    public static final int SHOW_QUEUE = 3;
    public static final int SHOW_RECOMMENDATIONS = 4;

    private static final String TAG = "SongAdapter";

    private ArrayList<localSong> songList;
    private LayoutInflater layoutInflater;
    private ArrayList<Artist> artistList;
    private ArrayList<localSong> playingQueue;
    private ArrayList<Pair<String, String>> recommendations;

    private int mode;

    public SongAdapter(Context context, ArrayList<localSong> songList, ArrayList<Artist> artistList){
        this.songList = songList;
        this.artistList = artistList;
        layoutInflater = LayoutInflater.from(context);
        mode = SHOW_SONGS;
        playingQueue = new ArrayList<localSong>();
        recommendations = new ArrayList<Pair<String, String>>();
    }

    public void setPlayingQueue(ArrayList<localSong> playingQueue){
        this.playingQueue = playingQueue;
    }

    public void setRecommendations(ArrayList<Pair<String, String>> recommendations) { this.recommendations = recommendations; }

    public boolean changeMode(int newMode){
        switch (newMode){
            case SHOW_SONGS:
            case SHOW_ARTISTS:
            case SHOW_PLAYLISTS:
            case SHOW_QUEUE:
            case SHOW_RECOMMENDATIONS:
                mode = newMode;
                return true;
        }
        return false;
    }

    public int getMode(){return mode;}

    @Override
    public int getCount() {
        switch (mode){
            case SHOW_SONGS:
                return songList.size();
            case SHOW_ARTISTS:
                return artistList.size();
            case SHOW_QUEUE:
                return playingQueue.size();
            case SHOW_RECOMMENDATIONS:
                return recommendations.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        switch (mode){
            case SHOW_SONGS:
                return songList.get(position);
            case SHOW_ARTISTS:
                return artistList.get(position);
            case SHOW_QUEUE:
                return playingQueue.get(position);
            case SHOW_RECOMMENDATIONS:
                return recommendations.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        switch(mode){
            case SHOW_SONGS:
                return songList.get(position).getId();
            case SHOW_QUEUE:
                return playingQueue.get(position).getId();
        }
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        switch (mode){
            case SHOW_SONGS:
                return showSongs(position, parent, songList);
            case SHOW_ARTISTS:
                return showArtist(position, parent);
            case SHOW_QUEUE:
                return showSongs(position, parent, playingQueue);
            case SHOW_RECOMMENDATIONS:
                return showRecommendations(position, parent, recommendations);
        }
        return null;
    }

    private LinearLayout showRecommendations(int position, ViewGroup parent, ArrayList<Pair<String, String>> list){
        LinearLayout linearLayout = (LinearLayout) layoutInflater.inflate(R.layout.recommendations, parent, false);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        TextView titleTV = (TextView) linearLayout.findViewById(R.id.recmdnSongName);
        TextView artistTV = (TextView) linearLayout.findViewById(R.id.recmdnArtistName);
        TextView linkTV = (TextView) linearLayout.findViewById(R.id.recmdnLink);
        linkTV.setMovementMethod(LinkMovementMethod.getInstance());
        String url = "https://www.youtube.com/results?search_query=";
        String songTitle = list.get(position).getLeft();
        String songArtist = list.get(position).getRight();
        String searchQuery = songTitle + " " + songArtist;
        searchQuery = searchQuery.replaceAll(" ", "+");
        url = url + searchQuery;
        String hyperlink = "<a href='" + url + "'> Link </a>";
        linkTV.setText(Html.fromHtml(hyperlink));
        artistTV.setText(songArtist);
        titleTV.setText(songTitle);
        linearLayout.setTag(position);
        return linearLayout;
    }

    private LinearLayout showSongs(int position, ViewGroup parent, ArrayList<localSong> list){
        LinearLayout linearLayout = (LinearLayout) layoutInflater.inflate(R.layout.song, parent, false);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        TextView titleTV = (TextView) linearLayout.findViewById(R.id.songName);
        TextView artistTV = (TextView) linearLayout.findViewById(R.id.songArtist);
        localSong song = list.get(position);
        titleTV.setText(song.getTitle());
        artistTV.setText(song.getArtist());
        linearLayout.setTag(position);
        return linearLayout;
    }

    private LinearLayout showArtist(int position, ViewGroup parent){
        LinearLayout linearLayout = (LinearLayout) layoutInflater.inflate(R.layout.artist, parent, false);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        TextView nameTV = (TextView) linearLayout.findViewById(R.id.artistName);
        TextView numberTV = (TextView) linearLayout.findViewById(R.id.numberOfSongs);
        Artist artist = artistList.get(position);
        nameTV.setText(artist.getName());
        numberTV.setText(String.valueOf(artist.getNumberOfSongs()) + " songs");
        linearLayout.setTag(position);
        return linearLayout;
    }
}

class SongDataReceiver extends BroadcastReceiver{

    public static final String TAG = "SongDataReceiver";

    private MusicController controller;
    private View view;

    public SongDataReceiver(MusicController controller, View view) {
        this.controller = controller;
        this.view = view;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra(MainActivity.TRANSMIT_TITLE);
        String artist = intent.getStringExtra(MainActivity.TRANSMIT_ARTIST);
        Log.d(TAG, "Received " + title + " - " + artist);
        //controller.setAnchorView(view, title, artist);
        view.findViewById(R.id.currentSongDetailsLinLayout).setVisibility(View.VISIBLE);
        TextView titleTV = (TextView) view.findViewById(R.id.songNameTV);
        TextView artistTV = (TextView) view.findViewById(R.id.artistNameTV);
        titleTV.setText(title);
        artistTV.setText(artist);
        RatingBar ratingBar = (RatingBar) view.findViewById(R.id.songRating);
        ratingBar.setRating(0.0f);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

            }
        });
    }
}