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

public class AudioService extends Service {
    public static final String ACTION_FETCH_COMPLETE = "com.example.tp8tdm.FETCH_COMPLETE";
    public static final String EXTRA_SONGS_LIST = "extra_songs_list";
    ArrayList<AudioModel> songsList = new ArrayList<>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        fetchAudioFromMediaStore();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void fetchAudioFromMediaStore() {
        // Projection for the query
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION
        };

        // Selection for audio files only
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        // Querying MediaStore for audio files
        Cursor cursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null);

        // Loop through the cursor to retrieve audio file information
        while (cursor != null && cursor.moveToNext()) {
            AudioModel songData = new AudioModel(
                    cursor.getString(1), // Path
                    cursor.getString(0), // Title
                    cursor.getString(2)); // Duration
            // Check if the file exists and add to the list
            if (new File(songData.getPath()).exists())
                songsList.add(songData);
        }
        Intent broadcastIntent = new Intent(ACTION_FETCH_COMPLETE);
        broadcastIntent.putExtra(EXTRA_SONGS_LIST, songsList);
        sendBroadcast(broadcastIntent);

        // Handle the fetched audio data here, you can store it or send it to another component.
    }
}

