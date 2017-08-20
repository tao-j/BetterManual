package com.obsidium.bettermanual.capture;

public interface CaptureModeInterface {
    void reset();
    void startCountDown();
    void prepare();
    void startShooting();
    void abort();

    boolean isActive();

    void onShutter(int i);

    void increment();
    void decrement();
    void incrementPicCount();
    void decrementPicCount();
}
