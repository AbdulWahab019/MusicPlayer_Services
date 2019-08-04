package com.example.musicplayer_services;

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
import android.util.Log;
import android.widget.SeekBar;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class myService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private final IBinder musicBind = new MusicBinder();
    private MediaPlayer player;
    private ArrayList<Song> songs;
    private int songPosition;
    private AudioManager audioManager;
    private float volume = 0;
    private int volumeLevel;
    private boolean muted = false;
    private SeekBar seekBar;

    public void onCreate(){
        super.onCreate();
        songPosition = 0;
        player = new MediaPlayer();

        initMusicPlayer();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

    }

    public void initMusicPlayer(){
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setList(ArrayList<Song> songs){
        this.songs = songs;
    }

    public void setSong(int index){
        songPosition = index;
    }

    public void playSong(){
        player.reset();

        Log.i("Playlist","Song Position");
        Song playSong = songs.get(songPosition);
        long currSong = playSong.getId();

        Uri trackUri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong
        );

        try{
            player.setDataSource(getApplicationContext(), trackUri);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        player.prepareAsync();

        seekBar = MainActivity.getSeekBar();
        seekBar.setMax(getDuration());
        seekBar.setProgress(0);
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                seekBar.setProgress(getPosition()/1000);

                if(seekBar.getProgress() >= getDuration()/1000){
                    playNext();
                }

            }
        },0,1000);
    }

    public int getPosition(){
        return player.getCurrentPosition();
    }

    public int getDuration(){
        return player.getDuration();
    }

    public boolean isPlaying(){
        return player.isPlaying();
    }

    public void pausePlayer(){
        player.pause();
    }

    public boolean mute(){
        if(muted){
            player.setVolume(volume, volume);
        }
        else {
            volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) / 15f;
            player.setVolume(0, 0);
        }

        muted = !muted;

        return muted;
    }
    public void volumeDown() {
        volumeLevel = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if(volumeLevel != 0)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (volumeLevel-1), 0);
    }

    public void volumeUp(){
        volumeLevel = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (volumeLevel+1), 0);
    }

    public void seek(int position){
        player.seekTo(position);
    }

    public void go(){
        player.start();
    }

    public void playPrev(){
        songPosition--;
        if(songPosition < 0)
            songPosition = songs.size() - 1;

        playSong();
    }

    public void playNext(){
        songPosition++;
        if(songPosition >= songs.size())
            songPosition = 0;

        playSong();
    }
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();

        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) { }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) { return false; }

    @Override
    public void onPrepared(MediaPlayer mp) { mp.start(); }

    public void seekBarHandler(SeekBar seekBar, int progress, boolean fromUser) {
        seekBar.setMax(player.getDuration() / 1000);

        if(fromUser)
            player.seekTo(progress*1000);

    }

    class MusicBinder extends Binder {
        myService getService() {
            return myService.this;
        }
    }
}