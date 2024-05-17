package com.example.tp8tdm;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import android.database.Cursor;
import android.provider.MediaStore;

import androidx.core.app.NotificationCompat;

import java.io.File;
public class AudioService extends Service {
    private static final String ACTION_STOP = "ACTION_STOP";
    private static final String ACTION_PREVIOUS = "ACTION_PREVIOUS";
    private static final String ACTION_PLAY_PAUSE = "ACTION_PLAY_PAUSE";
    private static final String ACTION_NEXT = "ACTION_NEXT";

    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "audio_service_channel";
    ArrayList<AudioModel> songsList = new ArrayList<>();
    AudioModel currentSong;
    MediaPlayer mediaPlayer = MyMediaPlayer.getInstance();

    private MusicPlayerActivity musicPlayerActivity;

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

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @SuppressLint("NewApi")
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Audio Service Channel", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Channel for Audio Service Notifications");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification() {

        Intent nextIntent = new Intent(this, AudioService.class);
        nextIntent.setAction(ACTION_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getService(this, 0, nextIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent playPauseIntent = new Intent(this, AudioService.class);
        playPauseIntent.setAction(ACTION_PLAY_PAUSE);
        PendingIntent playPausePendingIntent = PendingIntent.getService(this, 0, playPauseIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent previousIntent = new Intent(this, AudioService.class);
        previousIntent.setAction(ACTION_PREVIOUS);
        PendingIntent previousPendingIntent = PendingIntent.getService(this, 0, previousIntent, PendingIntent.FLAG_IMMUTABLE);




        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.music_icon)
                .setContentTitle(currentSong.getTitle())
                .addAction(R.drawable.ic_baseline_skip_previous_24, "Previous", previousPendingIntent)
                .addAction(R.drawable.ic_baseline_play_circle_outline_24, "Play/Pause", playPausePendingIntent)
                .addAction(R.drawable.ic_baseline_skip_next_24, "Next", nextPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        Notification notification = builder.build();
        startForeground(NOTIFICATION_ID, notification);
    }

    private void handleNotificationAction(String action) {
        Log.d("AudioService", "Notification action clicked: " + action);
        switch (action) {

            case ACTION_PREVIOUS:
                playPreviousSong();
                break;
            case ACTION_PLAY_PAUSE:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                } else {
                    mediaPlayer.start();
                }
                if (musicPlayerActivity != null) {
                    musicPlayerActivity.updatePlayPauseButton();
                }
                break;
            case ACTION_NEXT:
                playNextSong();
                break;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            handleNotificationAction(action);
        }
        return START_NOT_STICKY;
    }

    public ArrayList<AudioModel> fetchAudioFromMediaStore() {
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
            showNotification();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playNextSong() {
        if (MyMediaPlayer.currentIndex == songsList.size() - 1)
            return;
        MyMediaPlayer.currentIndex += 1;
        playMusic(songsList, MyMediaPlayer.currentIndex);
        updateTitle(currentSong.getTitle());
    }

    public void playPreviousSong() {
        if (MyMediaPlayer.currentIndex == 0)
            return;
        MyMediaPlayer.currentIndex -= 1;
        playMusic(songsList, MyMediaPlayer.currentIndex);
        updateTitle(currentSong.getTitle());
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
        mediaPlayer.reset(); // Reset the media player to its uninitialized state
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public String getCurrentSongDuration() {
        return currentSong.getDuration();
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

    public interface OnStopMusicListener {
        void onStopMusic();
    }

    private OnStopMusicListener onStopMusicListener;

    public void setOnStopMusicListener(OnStopMusicListener listener) {
        this.onStopMusicListener = listener;
    }
    public void setMusicPlayerActivity(MusicPlayerActivity activity) {
        this.musicPlayerActivity = activity;
    }

    private void updateTitle(String title) {
        if (musicPlayerActivity != null) {
            musicPlayerActivity.updateTitle(title);
        }
    }
}
