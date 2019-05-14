package com.example.haboker;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.example.haboker.XML.Segment;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class SegmentPlayer extends Service implements Player.EventListener, PlayerNotificationManager.MediaDescriptionAdapter {
    public static final String INTENT_NAME = "SEGMENT_PLAYER";
    public static final String PLAYING = "PLAYING";
    public static final String PAUSED = "PAUSED";
    public static final String BUFFERING = "BUFFERING";
    public static final String ENDED = "ENDED";
    public static final String TIME_UPDATED = "TIME_UPDATED";
    public static SegmentPlayer instance;
    private SimpleExoPlayer exoPlayer;
    private PlayerNotificationManager pnm;
    private boolean isPlaying = false;
    private TimerRunnable timerRunnable;
    private Segment currentSegment;


    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this);
        exoPlayer.addListener(this);
        timerRunnable = new TimerRunnable();
        timerRunnable.run();
        pnm = new PlayerNotificationManager(this, MainActivity.CHANNEL_ID, 0, this);
        pnm.setPlayer(exoPlayer);
        pnm.setUseNavigationActions(false);
        pnm.setFastForwardIncrementMs(10000);
        pnm.setRewindIncrementMs(10000);
        pnm.setStopAction(null);
        pnm.setNotificationListener(new PlayerNotificationManager.NotificationListener() {
            @Override
            public void onNotificationStarted(int notificationId, Notification notification) {
                startForeground(notificationId, notification);
            }

            @Override
            public void onNotificationCancelled(int notificationId) {
                stopSelf();
            }
        });
    }

    public static SegmentPlayer getInstance() {
        return instance;
    }

    public void start(Segment segment) {
        currentSegment = segment;
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "eco99fm"));
        MediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(segment.RecordedProgramsDownloadFile));
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
            sendTimeUpdateEvent(seekTo, exoPlayer.getBufferedPosition(), exoPlayer.getDuration());
        }
    }

    public void jumpBackwards() {
        if (isPlaying) {
            long seekTo = exoPlayer.getCurrentPosition() - 10000;
            if (seekTo < 0) {
                seekTo = 0;
            }

            exoPlayer.seekTo(seekTo);
            sendTimeUpdateEvent(seekTo, exoPlayer.getBufferedPosition(), exoPlayer.getDuration());
        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        System.out.println("Play when ready: " + playWhenReady);
        System.out.println("Playback state: " + playbackState);
        isPlaying = playWhenReady;
        if (playWhenReady && playbackState == Player.STATE_READY) {
            sendPlaybackEvent(PLAYING);
        } else if (playWhenReady) {
            if (playbackState == Player.STATE_BUFFERING) {
                sendPlaybackEvent(BUFFERING);
            } else {
                System.out.println("Something is wrong");
                ExoPlaybackException playbackError = exoPlayer.getPlaybackError();
                if (playbackError != null) {
                    System.out.println(playbackError.toString());
                }
                sendPlaybackEvent(ENDED);
                isPlaying = false;
            }
        } else {
            sendPlaybackEvent(PAUSED);
        }
    }

    @Override
    public void onDestroy() {
        pnm.setPlayer(null);
        exoPlayer.release();
        exoPlayer = null;

        super.onDestroy();
    }

    private void sendPlaybackEvent(String event) {
        Intent intent = new Intent(INTENT_NAME);
        intent.putExtra("status", event);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendTimeUpdateEvent(long progress, long buffered, long duration) {
        Intent intent = new Intent(INTENT_NAME);
        intent.putExtra("status", TIME_UPDATED);
        intent.putExtra("progress", progress);
        intent.putExtra("buffered", buffered);
        intent.putExtra("duration", duration);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public String getCurrentContentTitle(Player player) {
        if (currentSegment == null) {
            return null;
        }

        return currentSegment.RecordedProgramsName;
    }

    @Nullable
    @Override
    public PendingIntent createCurrentContentIntent(Player player) {
        return null;
    }

    @Nullable
    @Override
    public String getCurrentContentText(Player player) {
        return null;
    }

    @Nullable
    @Override
    public Bitmap getCurrentLargeIcon(Player player, final PlayerNotificationManager.BitmapCallback callback) {
        Picasso.get().load(currentSegment.RecordedProgramsImg).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                callback.onBitmap(bitmap);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
        return null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    class TimerRunnable implements Runnable {
        final private Handler handler = new Handler();

        @Override
        public void run() {
            if (isPlaying && exoPlayer.getDuration() > 0) {
                sendTimeUpdateEvent(exoPlayer.getCurrentPosition(), exoPlayer.getBufferedPosition(), exoPlayer.getDuration());
            }
            handler.postDelayed(this, 1000);
        }
    }
}
