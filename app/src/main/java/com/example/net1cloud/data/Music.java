package com.example.net1cloud.data;

import android.graphics.Bitmap;

import com.example.net1cloud.utils.TimeUtil;

import java.io.Serializable;

public class Music implements Serializable {

    private String name;

    private String artist;

    private String album;

    private String duration;

    private boolean isDownload;

    private String musicPath;

    private Bitmap albumImage;

    private boolean hasAlbumImage;

    private int durationInt;

    public Music(String musicPath) {
        this.musicPath = musicPath;
        isDownload = true;
        hasAlbumImage = false;
    }

    public void setDuration(String duration) {
        int musicDuration = Integer.parseInt(duration);
        this.durationInt = musicDuration;
        this.duration = TimeUtil.getTimeStrFromMilliSeconds(musicDuration);
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDownload() {
        return isDownload;
    }

    public void setDownload(boolean download) {
        isDownload = download;
    }

    public String getMusicPath() {
        return musicPath;
    }

    public void setMusicPath(String musicPath) {
        this.musicPath = musicPath;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public Bitmap getAlbumImage() {
        return albumImage;
    }

    public void setAlbumImage(Bitmap albumImage) {
        this.albumImage = albumImage;
    }

    public String getDuration() {
        return duration;
    }

    public boolean isHasAlbumImage() {
        return hasAlbumImage;
    }

    public void setHasAlbumImage(boolean hasAlbumImage) {
        this.hasAlbumImage = hasAlbumImage;
    }

    public int getDurationInt() { return durationInt; }
}
