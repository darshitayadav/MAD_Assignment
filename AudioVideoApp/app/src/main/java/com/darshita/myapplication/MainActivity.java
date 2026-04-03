package com.darshita.myapplication;


import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.VideoView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private VideoView videoView;
    private MediaPlayer mediaPlayer;
    private EditText editUrl;
    private boolean isVideoMode = false;

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri audioUri = result.getData().getData();
                    setupAudio(audioUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoView = findViewById(R.id.videoView);
        editUrl = findViewById(R.id.editUrl);

        Button btnOpenFile = findViewById(R.id.btnOpenFile);
        Button btnOpenUrl = findViewById(R.id.btnOpenUrl);
        Button btnPlay = findViewById(R.id.btnPlay);
        Button btnPause = findViewById(R.id.btnPause);
        Button btnStop = findViewById(R.id.btnStop);
        Button btnRestart = findViewById(R.id.btnRestart);

        btnOpenFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/*");
            filePickerLauncher.launch(intent);
        });

        btnOpenUrl.setOnClickListener(v -> {
            String url = editUrl.getText().toString().trim();
            if (!url.isEmpty()) {
                setupVideo(url);
            }
        });

        btnPlay.setOnClickListener(v -> playMedia());
        btnPause.setOnClickListener(v -> pauseMedia());
        btnStop.setOnClickListener(v -> stopMedia());
        btnRestart.setOnClickListener(v -> restartMedia());
    }

    private void setupAudio(Uri uri) {
        releaseMediaPlayer();
        isVideoMode = false;
        videoView.stopPlayback();
        videoView.setVisibility(View.GONE);

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(this, uri);
            mediaPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupVideo(String url) {
        releaseMediaPlayer();
        isVideoMode = true;
        videoView.setVisibility(View.VISIBLE);
        Uri uri = Uri.parse(url);
        videoView.setVideoURI(uri);
    }

    private void playMedia() {
        if (isVideoMode) {
            videoView.start();
        } else if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    private void pauseMedia() {
        if (isVideoMode && videoView.isPlaying()) {
            videoView.pause();
        } else if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    private void stopMedia() {
        if (isVideoMode) {
            videoView.pause();
            videoView.seekTo(0);
        } else if (mediaPlayer != null) {
            mediaPlayer.stop();
            try {
                mediaPlayer.prepare();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void restartMedia() {
        if (isVideoMode) {
            videoView.seekTo(0);
            videoView.start();
        } else if (mediaPlayer != null) {
            mediaPlayer.seekTo(0);
            mediaPlayer.start();
        }
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
    }
}