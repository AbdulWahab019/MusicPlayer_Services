package com.example.musicplayer_services;

class Song {
    private long id;
    private String title;
    private String artist;

    Song(long id, String title, String artist) {
        this.id = id;
        this.title = title;
        this.artist = artist;
    }

    long getId() {
        return id;
    }

    String getTitle() { return title; }

    String getArtist() {
        return artist;
    }
}
