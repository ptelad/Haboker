package com.ptelad.haboker;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.session.MediaSessionCompat;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.ptelad.haboker.XML.Segment;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class SegmentPlayer extends Service implements Player.EventListener, PlayerNotificationManager.MediaDescriptionAdapter {
    public static final String INTENT_NAME = "SEGMENT_PLAYER";
    public static final String PLAYING = "PLAYING";
    public static final String PAUSED = "PAUSED";
    public static final String BUFFERING = "BUFFERING";
    public static final String ENDED = "ENDED";
    public static final String TIME_UPDATED = "TIME_UPDATED";
    public static final String DISMISSED = "DISMISSED";
    public static SegmentPlayer instance;
    private SimpleExoPlayer exoPlayer;
    private PlayerNotificationManager pnm;
    private PowerManager.WakeLock wakeLock;
    private boolean isPlaying = false;
    private long duration = 0;
    private Segment currentSegment;
    private TimerRunnable timerRunnable;
    private MediaSessionCompat mediaSession;
    private MediaSessionConnector mediaSessionConnector;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        currentSegment = intent.getParcelableExtra("segment");
        instance = this;
//        loadSegment();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"Haboker::Playback");
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this);
        exoPlayer.addListener(this);
        timerRunnable = new TimerRunnable();
        timerRunnable.run();
        pnm = new PlayerNotificationManager(this, MainActivity.CHANNEL_ID, 101, this);
        pnm.setNotificationListener(new PlayerNotificationManager.NotificationListener() {
            @Override
            public void onNotificationStarted(int notificationId, Notification notification) {
                startForeground(notificationId, notification);
                System.out.println("called start forground!!!");
            }

            @Override
            public void onNotificationCancelled(int notificationId) {
                saveSegment();
                stopSelf();
            }
        });
        pnm.setPlayer(exoPlayer);
        pnm.setUseNavigationActions(false);
        pnm.setFastForwardIncrementMs(10000);
        pnm.setRewindIncrementMs(10000);
        pnm.setSmallIcon(R.drawable.ic_radio_icon);

        mediaSession = new MediaSessionCompat(this, "haboker");
        pnm.setMediaSessionToken(mediaSession.getSessionToken());
        mediaSessionConnector = new MediaSessionConnector(mediaSession);
        mediaSessionConnector.setPlayer(exoPlayer, null);
        mediaSessionConnector.setQueueNavigator(new MediaSessionConnector.QueueNavigator() {
            @Override
            public long getSupportedQueueNavigatorActions(@Nullable Player player) {
                return MediaSessionConnector.QueueNavigator.ACTIONS;
            }

            @Override
            public void onTimelineChanged(Player player) {

            }

            @Override
            public void onCurrentWindowIndexChanged(Player player) {

            }

            @Override
            public long getActiveQueueItemId(@Nullable Player player) {
                return 0;
            }

            @Override
            public void onSkipToPrevious(Player player) {
                jumpBackwards();
            }

            @Override
            public void onSkipToQueueItem(Player player, long id) {

            }

            @Override
            public void onSkipToNext(Player player) {
                jumpForward();
            }

            @Override
            public String[] getCommands() {
                return new String[0];
            }

            @Override
            public void onCommand(Player player, String command, Bundle extras, ResultReceiver cb) {
            }
        });
        start();
        return START_STICKY;
    }

    public static SegmentPlayer getInstance() {
        return instance;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        System.out.println("Task removed!!!");
        saveSegment();
        pnm.setPlayer(null);
        exoPlayer.release();
        exoPlayer = null;
        mediaSession.setActive(false);
        super.onTaskRemoved(rootIntent);
    }

    public void start() {
        if (!wakeLock.isHeld()) {
            wakeLock.acquire();
        }
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "eco99fm"));
        MediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(currentSegment.RecordedProgramsDownloadFile));
        exoPlayer.prepare(mediaSource);
        mediaSession.setActive(true);
        if (currentSegment.progress > 0) {
            exoPlayer.seekTo(currentSegment.progress);
        }
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

    private void saveSegment() {
        SharedPreferences sp = getSharedPreferences("haboker", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("url", currentSegment.RecordedProgramsDownloadFile);
        editor.putString("image", currentSegment.RecordedProgramsImg);
        editor.putString("title", currentSegment.RecordedProgramsName);
        editor.putLong("progress", currentSegment.progress);
        editor.putLong("duration", duration);
        editor.apply();
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        System.out.println("Play when ready: " + playWhenReady);
        System.out.println("Playback state: " + playbackState);
        isPlaying = playWhenReady;
        if (playWhenReady && playbackState == Player.STATE_READY) {
            sendPlaybackEvent(PLAYING);
            pnm.setOngoing(true);
        } else if (playWhenReady) {
            if (playbackState == Player.STATE_BUFFERING) {
                sendPlaybackEvent(BUFFERING);
            } else {
                System.out.println("Something is wrong");
                ExoPlaybackException playbackError = exoPlayer.getPlaybackError();
                if (playbackError != null) {
                    System.out.println(playbackError.toString());
                    start();
                } else {
                    sendPlaybackEvent(ENDED);
                    isPlaying = false;
                    mediaSession.setActive(false);
                    wakeLock.release();
                }
            }
        } else {
            sendPlaybackEvent(PAUSED);
            pnm.setOngoing(false);
        }
    }

    public long getTimeProgress() {
        return currentSegment.progress;
    }

    public long getDuration() {
        return duration;
    }

    @Override
    public void onDestroy() {
        System.out.println("Service destroyed!!!");
        pnm.setPlayer(null);
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
        mediaSession.setActive(false);
        saveSegment();
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }

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
//        Picasso.get().load(currentSegment.RecordedProgramsImg).into(new Target() {
//            @Override
//            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                callback.onBitmap(bitmap);
//            }
//
//            @Override
//            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
//
//            }
//
//            @Override
//            public void onPrepareLoad(Drawable placeHolderDrawable) {
//
//            }
//        });
        return null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    class TimerRunnable implements Runnable {
        final private Handler handler = new Handler();

        @Override
        public void run() {
            if (isPlaying && exoPlayer != null && exoPlayer.getDuration() > 0) {
                currentSegment.progress = exoPlayer.getContentPosition();
                duration = exoPlayer.getDuration();
                sendTimeUpdateEvent(currentSegment.progress, exoPlayer.getBufferedPosition(), duration);
            }
            handler.postDelayed(this, 1000);
        }
    }
}
