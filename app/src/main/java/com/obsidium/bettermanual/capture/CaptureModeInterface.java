package com.obsidium.bettermanual.capture;

interface CaptureModeInterface {
    void reset();
    void startCountDown();
    boolean prepare();
    void startShooting();
    void abort();

    boolean isActive();


    void incrementInterval();
    void decrementInterval();
    void incrementPicCount();
    void decrementPicCount();
}
