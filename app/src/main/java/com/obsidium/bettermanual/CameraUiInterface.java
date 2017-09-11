package com.obsidium.bettermanual;

import com.github.killerink.ActivityInterface;
import com.github.killerink.camera.CaptureSession;
import com.obsidium.bettermanual.views.ApertureView;
import com.obsidium.bettermanual.views.DriveMode;
import com.obsidium.bettermanual.views.ExposureModeView;
import com.obsidium.bettermanual.views.IsoView;
import com.obsidium.bettermanual.views.ShutterView;

public interface CameraUiInterface extends CaptureSession.CaptureDoneEvent
{
    void showMessageDelayed(String msg);
    void showMessage(String msg);
    void hideMessage();
    void showHintMessage(String msg);
    void hideHintMessage();
    int getActiveViewsFlag();
    void setActiveViewFlag(int viewsToShow);

    ExposureModeView getExposureMode();

    void updateViewVisibility();
    void setLeftViewVisibility(boolean state);
    ShutterView getShutter();
    DriveMode getDriveMode();
    ApertureView getAperture();
    IsoView getIso();

    ActivityInterface getActivityInterface();

}
