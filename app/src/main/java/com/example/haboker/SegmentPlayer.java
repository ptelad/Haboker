package com.example.haboker;

import android.content.Context;
import android.net.Uri;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class SegmentPlayer implements Player.EventListener {
    private Context context;
    private SimpleExoPlayer exoPlayer;
    private PlayerListener listener;
    private boolean isPlaying = false;

    public SegmentPlayer(Context context, PlayerListener listener) {
        this.context = context;
        this.listener = listener;
        exoPlayer = ExoPlayerFactory.newSimpleInstance(context);
        exoPlayer.addListener(this);
    }

    public void start(String url) {
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this.context, Util.getUserAgent(context, "eco99fm"));
        MediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(url));
        exoPlayer.prepare(mediaSource);
        exoPlayer.setPlayWhenReady(true);
    }

    public boolean playPause() {
        if (isPlaying) {
            exoPlayer.setPlayWhenReady(false);
        } else {
            exoPlayer.setPlayWhenReady(true);
        }

        return !isPlaying;
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        System.out.println("Play when ready: " + playWhenReady);
        System.out.println("Playback state: " + playbackState);
        isPlaying = playWhenReady;
        if (playWhenReady && playbackState == Player.STATE_READY) {
            listener.onPlaying();
        } else if (playWhenReady) {
            System.out.println("Something is wrong");
        } else {
            listener.onPaused();
        }
    }
}
