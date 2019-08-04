package com.example.musicplayer_services;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SongAdapter extends BaseAdapter {
    private ArrayList<Song> songs;
    private LayoutInflater songInformation;

    SongAdapter(Context c, ArrayList<Song> songs){
        this.songs = songs;
        this.songInformation = LayoutInflater.from(c);
    }

    public void setSongs(ArrayList<Song> songs){ this.songs = songs; }
    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        @SuppressLint("ViewHolder")
        LinearLayout songLayout = (LinearLayout) songInformation.inflate(R.layout.song, parent, false);
        TextView songView = songLayout.findViewById(R.id.song_title);
        TextView artistView = songLayout.findViewById(R.id.song_artist);

        Song currSong = songs.get(position);

        songView.setText(currSong.getTitle());
        artistView.setText(currSong.getArtist());

        songLayout.setTag(position);
        return songLayout;
    }
}
