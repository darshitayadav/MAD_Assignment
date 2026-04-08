package com.darshita.myapplication;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;

public class AudioPlayer {

    private MediaPlayer mediaPlayer;
    private OnPlaybackStateChangeListener stateChangeListener;

    public interface OnPlaybackStateChangeListener {
        void onPrepared();
        void onError(String error);
        void onCompletion();
    }

    public AudioPlayer(Context context) {
    }

    public void setOnPlaybackStateChangeListener(OnPlaybackStateChangeListener listener) {
        this.stateChangeListener = listener;
    }

    public void prepareFromUri(Uri uri) {
        release();
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build());
            mediaPlayer.setDataSource(uri.toString());
            mediaPlayer.setOnCompletionListener(mp -> {
                if (stateChangeListener != null) stateChangeListener.onCompletion();
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                if (stateChangeListener != null) stateChangeListener.onError("Error: " + what);
                return true;
            });
            mediaPlayer.setOnPreparedListener(mp -> {
                if (stateChangeListener != null) stateChangeListener.onPrepared();
            });
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            if (stateChangeListener != null) stateChangeListener.onError(e.getMessage());
        }
    }

    public void play() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            try {
                mediaPlayer.prepareAsync();
            } catch (Exception ignored) {
            }
        }
    }

    public void restart() {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(0);
            mediaPlayer.start();
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public void release() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
