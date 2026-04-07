package com.darshita.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private VideoView videoView;
    private EditText editUrl;
    private TextView txtAudioStatus;
    private boolean isVideoMode = false;
    private AudioPlayer audioPlayer;

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) setupAudio(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoView = findViewById(R.id.videoView);
        editUrl = findViewById(R.id.editUrl);
        txtAudioStatus = findViewById(R.id.txtAudioStatus);

        audioPlayer = new AudioPlayer(this);
        audioPlayer.setOnPlaybackStateChangeListener(new AudioPlayer.OnPlaybackStateChangeListener() {
            @Override
            public void onPrepared() {
                updateAudioStatus("Ready");
            }

            @Override
            public void onError(String error) {
                updateAudioStatus("Error");
                Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCompletion() {
                updateAudioStatus("Completed");
            }
        });

        findViewById(R.id.btnOpenFile).setOnClickListener(v -> openAudioFile());
        findViewById(R.id.btnOpenUrl).setOnClickListener(v -> openVideoUrl());
        findViewById(R.id.btnPlay).setOnClickListener(v -> playMedia());
        findViewById(R.id.btnPause).setOnClickListener(v -> pauseMedia());
        findViewById(R.id.btnStop).setOnClickListener(v -> stopMedia());
        findViewById(R.id.btnRestart).setOnClickListener(v -> restartMedia());
    }

    private void openAudioFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        filePickerLauncher.launch(intent);
    }

    private void openVideoUrl() {
        String url = editUrl.getText().toString().trim();
        if (!url.isEmpty()) {
            setupVideo(url);
        } else {
            Toast.makeText(this, "Enter URL", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupAudio(Uri uri) {
        releaseMediaPlayer();
        isVideoMode = false;
        videoView.stopPlayback();
        videoView.setVisibility(android.view.View.GONE);
        txtAudioStatus.setVisibility(android.view.View.VISIBLE);
        updateAudioStatus("Loading...");
        audioPlayer.prepareFromUri(uri);
    }

    private void setupVideo(String url) {
        releaseMediaPlayer();
        isVideoMode = true;
        txtAudioStatus.setVisibility(android.view.View.GONE);
        videoView.setVisibility(android.view.View.VISIBLE);
        videoView.setVideoURI(Uri.parse(url));
    }

    private void playMedia() {
        if (isVideoMode) {
            videoView.start();
        } else {
            audioPlayer.play();
            updateAudioStatus("Playing");
        }
    }

    private void pauseMedia() {
        if (isVideoMode) {
            if (videoView.isPlaying()) videoView.pause();
        } else {
            audioPlayer.pause();
            updateAudioStatus("Paused");
        }
    }

    private void stopMedia() {
        if (isVideoMode) {
            videoView.pause();
            videoView.seekTo(0);
        } else {
            audioPlayer.stop();
            updateAudioStatus("Stopped");
        }
    }

    private void restartMedia() {
        if (isVideoMode) {
            videoView.seekTo(0);
            videoView.start();
        } else {
            audioPlayer.restart();
            updateAudioStatus("Playing");
        }
    }

    private void updateAudioStatus(String status) {
        txtAudioStatus.setText("Status: " + status);
    }

    private void releaseMediaPlayer() {
        if (audioPlayer != null) audioPlayer.release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
    }
}
