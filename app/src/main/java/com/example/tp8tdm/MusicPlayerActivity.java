package com.example.tp8tdm;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MusicPlayerActivity extends AppCompatActivity {

    TextView titleTv, currentTimeTv, totalTimeTv;
    SeekBar seekBar;
    ImageView pausePlay, nextBtn, previousBtn, musicIcon;
    ArrayList<AudioModel> songsList;
    AudioModel currentSong;
    AudioService audioService;
    boolean isBound = false;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioService.AudioServiceBinder binder = (AudioService.AudioServiceBinder) service;
            audioService = binder.getService();
            isBound = true;
            initializeUI();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        titleTv = findViewById(R.id.song_title);
        currentTimeTv = findViewById(R.id.current_time);
        totalTimeTv = findViewById(R.id.total_time);
        seekBar = findViewById(R.id.seek_bar);
        pausePlay = findViewById(R.id.pause_play);
        nextBtn = findViewById(R.id.next);
        previousBtn = findViewById(R.id.previous);
        musicIcon = findViewById(R.id.music_icon_big);

        Intent intent = new Intent(this, AudioService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    private void initializeUI() {
        if (isBound) {
            songsList = audioService.fetchAudioFromMediaStore();
            audioService.playMusic(songsList, MyMediaPlayer.currentIndex);
            updateTitle();
            pausePlay.setOnClickListener(v -> pausePlay());
            nextBtn.setOnClickListener(v -> playNextSong());
            previousBtn.setOnClickListener(v -> playPreviousSong());

        }
    }
    private void pausePlay() {
        if (isBound) {
            audioService.pausePlay();
            updatePlayPauseButton();
        }
    }
    private void updatePlayPauseButton() {
        if (isBound) {
            if (audioService.isPlaying()) {
                pausePlay.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
            } else {
                pausePlay.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
            }
        }
    }

    // Method to play the next song
    private void playNextSong() {
        if (isBound) {
            audioService.playNextSong();
            updateTitle();
            updatePlayPauseButton();// Update the title after playing the next song
        }
    }
    // Method to play the previous song
    private void playPreviousSong() {
        if (isBound) {
            audioService.playPreviousSong();
            updateTitle();
            updatePlayPauseButton();
        }
    }
    private void updateTitle() {
        if (isBound) {
            String currentSongTitle = audioService.getCurrentSongTitle();
            titleTv.setText(currentSongTitle);
        }
    }


}
