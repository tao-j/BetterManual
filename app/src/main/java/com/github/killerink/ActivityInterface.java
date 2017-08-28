package com.github.killerink;

import android.os.Handler;

import com.obsidium.bettermanual.Preferences;
import com.sony.scalar.hardware.avio.DisplayManager;

/**
 * Created by troop on 27.08.2017.
 */

public interface ActivityInterface {
    boolean hasTouchScreen();
    Preferences getPreferences();
    KeyEventHandler getDialHandler();
    CameraWrapper getCamera();
    Handler getMainHandler();
    Handler getBackHandler();
    DisplayManager getDisplayManager();
    void closeApp();
    void setColorDepth(boolean highQuality);
    void loadFragment(int fragment);
}
