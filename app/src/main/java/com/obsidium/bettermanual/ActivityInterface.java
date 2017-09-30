package com.obsidium.bettermanual;

import android.os.Handler;
import android.view.View;

import com.obsidium.bettermanual.camera.CameraInstance;
import com.obsidium.bettermanual.camera.CaptureSession;
import com.sony.scalar.hardware.avio.DisplayManager;

/**
 * Created by KillerInk on 27.08.2017.
 */

public interface ActivityInterface {
    boolean hasTouchScreen();
    Preferences getPreferences();
    KeyEventHandler getDialHandler();
    CameraInstance getCamera();
    Handler getMainHandler();
    DisplayManager getDisplayManager();
    void closeApp();
    void setColorDepth(boolean highQuality);
    void loadFragment(int fragment);
    void setSurfaceViewOnTouchListner(View.OnTouchListener onTouchListner);
    String getResString(int id);
    void setCaptureDoneEventListner(CaptureSession.CaptureDoneEvent eventListner);
    boolean isCaptureInProgress();
    boolean isBulbCapture();
    void cancelBulbCapture();
    void setBulbCapture(boolean bulbCapture);
    AvIndexHandler getAvIndexHandler();

}
