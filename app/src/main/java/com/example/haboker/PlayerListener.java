package com.example.haboker;

public interface PlayerListener {
    void onPlaying();
    void onPaused();
    void onBuffering();
    void onEnded();
    void onTimeUpdate(long position, long buffered, long duration);
}
