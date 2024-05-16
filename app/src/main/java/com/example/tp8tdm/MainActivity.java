package com.example.tp8tdm;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public static final String ACTION_FETCH_COMPLETE = "com.example.tp8tdm.FETCH_COMPLETE";
    public static final String EXTRA_SONGS_LIST = "extra_songs_list";
    RecyclerView recyclerView;
    TextView noMusicTextView;
    ArrayList<AudioModel> songsList = new ArrayList<>();

    private BroadcastReceiver fetchCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(ACTION_FETCH_COMPLETE)) {
                ArrayList<AudioModel> receivedSongsList = (ArrayList<AudioModel>) intent.getSerializableExtra(EXTRA_SONGS_LIST);
                if (receivedSongsList != null) {
                    songsList.clear();
                    songsList.addAll(receivedSongsList);
                    updateUI();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_view);
        noMusicTextView = findViewById(R.id.no_songs_text);

        // Register broadcast receiver
        registerReceiver(fetchCompleteReceiver, new IntentFilter(AudioService.ACTION_FETCH_COMPLETE));

        // Start the AudioService
        Intent serviceIntent = new Intent(this, AudioService.class);
        startService(serviceIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update UI with the fetched songsList
        updateUI();
    }

    private void updateUI() {
        // If songsList is empty, show noMusicTextView, otherwise, populate RecyclerView
        if (songsList.isEmpty()) {
            noMusicTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            noMusicTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            // Set up RecyclerView
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new MusicListAdapter(songsList, getApplicationContext()));
        }
    }

}
