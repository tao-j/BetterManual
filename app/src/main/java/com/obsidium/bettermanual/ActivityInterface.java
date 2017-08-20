package com.obsidium.bettermanual;

import android.os.Handler;

import com.obsidium.bettermanual.views.ApertureView;
import com.obsidium.bettermanual.views.DriveMode;
import com.obsidium.bettermanual.views.ExposureModeView;
import com.obsidium.bettermanual.views.IsoView;
import com.obsidium.bettermanual.views.ShutterView;
import com.sony.scalar.hardware.CameraEx;

public interface ActivityInterface
{
    void showMessageDelayed(String msg);
    void showMessage(String msg);
    void hideMessage();
    void showHintMessage(String msg);
    void hideHintMessage();
    int getActiveViewsFlag();
    void setActiveViewFlag(int viewsToShow);
    Preferences getPreferences();
    CameraEx getCamera();
    ExposureModeView getExposureMode();
    Handler getMainHandler();
    Handler getBackHandler();
    void updateViewVisibility();
    void setLeftViewVisibility(boolean state);
    void setDialMode(ManualActivity.DialMode dialMode);
    ManualActivity.DialMode getDialMode();
    ShutterView getShutter();
    DriveMode getDriveMode();
    ApertureView getAperture();
    IsoView getIso();
    void startActivity(Class<?> activity);
}
