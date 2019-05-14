package com.example.haboker;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.metadata.MetadataOutput;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class SegmentPlayer implements Player.EventListener {
    private Context context;
    private SimpleExoPlayer exoPlayer;
    private PlayerListener listener;
    private boolean isPlaying = false;
    private TimerRunnable timerRunnable;

    public SegmentPlayer(Context context, PlayerListener listener) {
        this.context = context;
        this.listener = listener;
        exoPlayer = ExoPlayerFactory.newSimpleInstance(context);
        exoPlayer.addListener(this);
        timerRunnable = new TimerRunnable();
        timerRunnable.run();
    }

    public void start(String url) {
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this.context, Util.getUserAgent(context, "eco99fm"));
        MediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(url));
        exoPlayer.prepare(mediaSource);
        exoPlayer.setPlayWhenReady(true);
    }

    public void playPause() {
        if (isPlaying) {
            exoPlayer.setPlayWhenReady(false);
        } else {
            exoPlayer.setPlayWhenReady(true);
        }
    }

    public void seek(float percent) {
        exoPlayer.seekTo((long)(exoPlayer.getDuration() * percent));
    }

    public void jumpForward() {
        if (isPlaying) {
            long seekTo = exoPlayer.getCurrentPosition() + 10000;
            if (seekTo > exoPlayer.getDuration()) {
                seekTo = exoPlayer.getDuration();
            }

            exoPlayer.seekTo(seekTo);
            listener.onTimeUpdate(seekTo, exoPlayer.getBufferedPosition(), exoPlayer.getDuration());
        }
    }

    public void jumpBackwards() {
        if (isPlaying) {
            long seekTo = exoPlayer.getCurrentPosition() - 10000;
            if (seekTo < 0) {
                seekTo = 0;
            }

            exoPlayer.seekTo(seekTo);
            listener.onTimeUpdate(seekTo, exoPlayer.getBufferedPosition(), exoPlayer.getDuration());
        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        System.out.println("Play when ready: " + playWhenReady);
        System.out.println("Playback state: " + playbackState);
        isPlaying = playWhenReady;
        if (playWhenReady && playbackState == Player.STATE_READY) {
            listener.onPlaying();
        } else if (playWhenReady) {
            if (playbackState == Player.STATE_BUFFERING) {
                listener.onBuffering();
            } else {
                System.out.println("Something is wrong");
                ExoPlaybackException playbackError = exoPlayer.getPlaybackError();
                if (playbackError != null) {
                    System.out.println(playbackError.toString());
                }
                listener.onEnded();
                isPlaying = false;
            }
        } else {
            listener.onPaused();
        }
    }

    class TimerRunnable implements Runnable {
        final private Handler handler = new Handler();

        @Override
        public void run() {
            if (isPlaying && exoPlayer.getDuration() > 0) {
                listener.onTimeUpdate(exoPlayer.getCurrentPosition(), exoPlayer.getBufferedPosition(), exoPlayer.getDuration());
            }
            handler.postDelayed(this, 1000);
        }
    }
}
