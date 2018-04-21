package com.obsidium.bettermanual.capture;

import com.obsidium.bettermanual.controller.Controller;
import com.obsidium.bettermanual.model.Model;

interface CaptureModeInterface {
    void reset();
    void startCountDown();
    boolean prepare();
    void startShooting();
    void abort();

    boolean isActive();


    void increment();
    void decrement();
    void incrementPicCount();
    void decrementPicCount();
}
