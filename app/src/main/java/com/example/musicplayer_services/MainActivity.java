package com.example.musicplayer_services;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.MediaController.MediaPlayerControl;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.view.View;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;


public class MainActivity extends AppCompatActivity implements MediaPlayerControl{

    ArrayList<Song> songList;
    ArrayList<Song> playlistSongs = new ArrayList<>();
    ArrayList<Song> unPlaylistSongs = new ArrayList<>();
    LinearLayout listLayout;
    ListView songView;
    SongAdapter songAdapter;
    myService musicService;
    Intent playIntent;
    MusicController controller;
    ImageButton previousButton, playButton, nextButton, muteButton, volumeUpButton, volumeDownButton;
    Switch playlistToggle;
    Button addSongPlaylist, removeSongPlaylist;
    @SuppressLint("StaticFieldLeak")
    static SeekBar seekBar;
    int index;
    boolean musicBound = false, muteStatus, addButtonPressed = false, removeButtonPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        songView = findViewById(R.id.songList);
        previousButton = findViewById(R.id.previous);
        playButton = findViewById(R.id.play);
        nextButton = findViewById(R.id.next);
        muteButton = findViewById(R.id.mute);
        volumeUpButton = findViewById(R.id.volumeUp);
        volumeDownButton = findViewById(R.id.volumeDown);
        seekBar = findViewById(R.id.seek);
        playlistToggle = findViewById(R.id.playlistToggle);
        addSongPlaylist = findViewById(R.id.addSong);
        removeSongPlaylist = findViewById(R.id.removeSong);
        listLayout = findViewById(R.id.layoutList);

        songList = new ArrayList<>();
        getSongList();
        sortSongs();
        unPlaylistSongs = songList;

        songAdapter = new SongAdapter(this, songList);
        songView.setAdapter(songAdapter);

        setController();
        onListeners();
    }

    private void onListeners(){
        previousButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                playPrev();
                playButton.setImageResource(R.drawable.pause_icon);
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                playNext();
                playButton.setImageResource(R.drawable.pause_icon);
            }
        });

        playButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(isPlaying()){
                    pause();
                    playButton.setImageResource(R.drawable.play_icon);
                }
                else{
                   start();
                   playButton.setImageResource(R.drawable.pause_icon);
                }
            }
        });

        muteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                muteStatus = musicService.mute();
                if(muteStatus){
                    muteButton.setImageResource(R.drawable.unmute_icon);
                }
                else{
                    muteButton.setImageResource(R.drawable.mute_icon);
                }
            }
        });

        volumeDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicService.volumeDown();
            }
        });

        volumeUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                musicService.volumeUp();
            }});

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                musicService.seekBarHandler(seekBar, progress, fromUser);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        playlistToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(playlistToggle.isChecked()) {
                    playlistOn();
                }
                else{
                    songView.setBackgroundColor(Color.parseColor("#979797"));
                    listLayout.setBackgroundColor(Color.parseColor("#979797"));

                    songList = new ArrayList<>();
                    getSongList();
                    sortSongs();
                    songAdapter.setSongs(songList);
                    songView.setAdapter(songAdapter);
                    musicService.setList(songList);
                    addSongPlaylist.setVisibility(View.INVISIBLE);
                    removeSongPlaylist.setVisibility(View.INVISIBLE);
                }
            }
        });

        addSongPlaylist.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                songView.setAdapter(null);

                songAdapter.setSongs(unPlaylistSongs);
                sortSongs();
                songView.setAdapter(songAdapter);
                songView.setBackgroundColor(Color.DKGRAY);
                listLayout.setBackgroundColor(Color.DKGRAY);

                removeButtonPressed = false;
                addButtonPressed = true;
            }
        });

        removeSongPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                songView.setBackgroundColor(Color.DKGRAY);
                listLayout.setBackgroundColor(Color.DKGRAY);

                addButtonPressed = false;
                removeButtonPressed = true;

                songView.setAdapter(null);
                songAdapter.setSongs(playlistSongs);
                sortSongs();
                songView.setAdapter(songAdapter);
                musicService.setList(playlistSongs);
            }
        });
    }

    private void playlistOn(){
        songView.setAdapter(null);

        addSongPlaylist.setVisibility(View.VISIBLE);
        removeSongPlaylist.setVisibility(View.VISIBLE);

        songView.setBackgroundColor(Color.parseColor("#979797"));
        listLayout.setBackgroundColor(Color.parseColor("#979797"));

        songAdapter.setSongs(playlistSongs);
        sortSongs();
        songView.setAdapter(songAdapter);
        musicService.setList(playlistSongs);
    }

    private void sortSongs(){
        Collections.sort(songList, new Comparator<Song>() {
            @Override
            public int compare(Song a, Song b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
        if(playIntent == null){
            playIntent = new Intent(this, myService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    @Override
    protected void onDestroy(){
        stopService(playIntent);
        musicService = null;
        super.onDestroy();
    }

    public void songPicked(View view){
        if(playlistToggle.isChecked() && addButtonPressed){
            index = Integer.parseInt(view.getTag().toString());

            playlistSongs.add(unPlaylistSongs.get(index));
            unPlaylistSongs.remove(index);

            addButtonPressed = false;
            playlistOn();
        }
        else if(playlistToggle.isChecked() && removeButtonPressed){
            index = Integer.parseInt(view.getTag().toString());
            unPlaylistSongs.add(playlistSongs.get(index));
            playlistSongs.remove(index);

            removeButtonPressed = false;
            playlistOn();
        }
        else{
            musicService.setSong(Integer.parseInt(view.getTag().toString()));
            musicService.playSong();
            playButton.setImageResource(R.drawable.pause_icon);
        }
    }

    public void getSongList(){
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        @SuppressLint("Recycle") Cursor musicCursor = musicResolver.query(musicUri, null, null,null,null);

        if(musicCursor != null && musicCursor.moveToFirst()){
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);

            do {
                long songId = musicCursor.getLong(idColumn);
                String songTitle = musicCursor.getString(titleColumn);
                String songArtist = musicCursor.getString(artistColumn);
                songList.add(new Song(songId, songTitle, songArtist));
            } while(musicCursor.moveToNext());
        }
    }

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myService.MusicBinder binder = (myService.MusicBinder) service;

            musicService = binder.getService();

            musicService.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    private void setController(){
        controller = new MusicController(this);

        controller.setPrevNextListeners(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener(){
            @Override
            public void onClick(View v){
                playPrev();
            }
        });

        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.songList));
        controller.setEnabled(true);
    }

    public static SeekBar getSeekBar(){
        return seekBar;
    }

    private void playNext(){
        musicService.playNext();
    }

    private void playPrev(){
        musicService.playPrev();
    }

    // Media Control Methods
    @Override
    public void start() {
        musicService.go();
    }

    @Override
    public void pause() {
        musicService.pausePlayer();
    }

    @Override
    public int getDuration() {
        if(musicService != null && musicBound && musicService.isPlaying())
            return musicService.getDuration();
        else
            return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(musicService != null && musicBound && musicService.isPlaying())
            return musicService.getPosition();
        else
            return 0;
    }

    @Override
    public void seekTo(int pos) {
        musicService.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if(musicService != null && musicBound)
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
}
