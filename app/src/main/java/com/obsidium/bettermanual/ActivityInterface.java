package com.obsidium.bettermanual;

import android.os.Handler;
import android.view.View;

import com.obsidium.bettermanual.camera.CaptureSession;
import com.sony.scalar.hardware.avio.DisplayManager;

/**
 * Created by KillerInk on 27.08.2017.
 */

public interface ActivityInterface {
    KeyEventHandler getDialHandler();
    Handler getMainHandler();
    String getResString(int id);
    DisplayManager getDisplayManager();
    boolean hasTouchScreen();
    void closeApp();
    void setColorDepth(boolean highQuality);
    void loadFragment(int fragment);
    void setSurfaceViewOnTouchListner(View.OnTouchListener onTouchListner);
    void setCaptureDoneEventListner(CaptureSession.CaptureDoneEvent eventListner);

    boolean isCaptureInProgress();

    boolean isBulbCapture();
    void cancelBulbCapture();
    void setBulbCapture(boolean bulbCapture);

    AvIndexManager getAvIndexManager();
}

