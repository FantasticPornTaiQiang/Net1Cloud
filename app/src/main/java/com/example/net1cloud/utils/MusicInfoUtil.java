package com.example.net1cloud.utils;

import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import com.example.net1cloud.data.Music;
import java.util.ArrayList;
import java.util.List;

public class MusicInfoUtil {

    public static String NowMusicPath = "";

    public static Music getMusic(String musicPath) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(musicPath);
        Music music = new Music(musicPath);
        music.setName(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
        music.setArtist(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
        music.setAlbum(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
        music.setDuration(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        try{
            music.setAlbumImage(BitmapFactory.decodeByteArray(mediaMetadataRetriever.getEmbeddedPicture(), 0, mediaMetadataRetriever.getEmbeddedPicture().length));
            music.setHasAlbumImage(true);
        } catch (NullPointerException e) {
            music.setAlbumImage(null);
            e.printStackTrace();
        } catch (Exception e) {
            music.setAlbumImage(null);
        }
        mediaMetadataRetriever.release();
        return music;
    }

    public static List<Music> getMusics(List<String> musicPathList) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        List<Music> musicList = new ArrayList<>();

        if(musicPathList != null && musicPathList.size() > 0) {
            for(String musicPath : musicPathList) {
                mediaMetadataRetriever.setDataSource(musicPath);
                Music music = new Music(musicPath);
                music.setName(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
                music.setArtist(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
                music.setAlbum(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
                musicList.add(music);
            }
        }

        return musicList;
    }

}
