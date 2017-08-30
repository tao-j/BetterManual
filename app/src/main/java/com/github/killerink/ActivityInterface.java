package com.github.killerink;

import android.os.Handler;

import com.github.killerink.camera.CameraInstance;
import com.obsidium.bettermanual.Preferences;
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
    Handler getBackHandler();
    DisplayManager getDisplayManager();
    void closeApp();
    void setColorDepth(boolean highQuality);
    void loadFragment(int fragment);
}
