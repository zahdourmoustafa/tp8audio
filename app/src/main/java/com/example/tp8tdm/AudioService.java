package com.example.tp8tdm;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcelable;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.util.ArrayList;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.database.Cursor;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;



import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class AudioService extends Service {

    ArrayList<AudioModel> songsList = new ArrayList<>();
    AudioModel currentSong;
    MediaPlayer mediaPlayer = MyMediaPlayer.getInstance();

    public class AudioServiceBinder extends Binder {
        AudioService getService() {
            return AudioService.this;
        }
    }

    private final IBinder binder = new AudioServiceBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public ArrayList<AudioModel> fetchAudioFromMediaStore() {
        // Fetch audio from media store
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION
        };
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null);
        while (cursor != null && cursor.moveToNext()) {
            AudioModel songData = new AudioModel(cursor.getString(1), cursor.getString(0), cursor.getString(2));
            if (new File(songData.getPath()).exists())
                songsList.add(songData);
        }
        return songsList;
    }

    public void playMusic(ArrayList<AudioModel> songsList, int index) {
        this.songsList = songsList;
        MyMediaPlayer.currentIndex = index;
        currentSong = songsList.get(MyMediaPlayer.currentIndex);
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(currentSong.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playNextSong() {
        if (MyMediaPlayer.currentIndex == songsList.size() - 1)
            return;
        MyMediaPlayer.currentIndex += 1;
        playMusic(songsList, MyMediaPlayer.currentIndex);
    }

    public void playPreviousSong() {
        if (MyMediaPlayer.currentIndex == 0)
            return;
        MyMediaPlayer.currentIndex -= 1;
        playMusic(songsList, MyMediaPlayer.currentIndex);
    }

    public void pausePlay() {
        if (mediaPlayer.isPlaying())
            mediaPlayer.pause();
        else
            mediaPlayer.start();
    }

    public void stopMusic() {
        if (mediaPlayer.isPlaying())
            mediaPlayer.stop();
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public void seekTo(int position) {
        mediaPlayer.seekTo(position);
    }
    public void setResourcesWithMusic(TextView titleTv, TextView totalTimeTv, ImageView pausePlay, ImageView nextBtn, ImageView previousBtn) {
        if (currentSong != null) {
            titleTv.setText(currentSong.getTitle());
            totalTimeTv.setText(convertToMMSS(currentSong.getDuration()));

            pausePlay.setOnClickListener(v -> pausePlay());
            nextBtn.setOnClickListener(v -> playNextSong());
            previousBtn.setOnClickListener(v -> playPreviousSong());
        }
    }

    private String convertToMMSS(String duration) {
        Long millis = Long.parseLong(duration);
        return String.format("%02d:%02d",
                (millis / 1000) / 60,
                (millis / 1000) % 60);
    }
    public String getCurrentSongTitle() {
        return currentSong != null ? currentSong.getTitle() : "";
    }
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }
}


