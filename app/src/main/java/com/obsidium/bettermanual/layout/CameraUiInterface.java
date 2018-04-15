package com.obsidium.bettermanual.layout;

import com.obsidium.bettermanual.ActivityInterface;
import com.obsidium.bettermanual.views.DriveMode;
import com.obsidium.bettermanual.views.ExposureModeView;

public interface CameraUiInterface
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
    DriveMode getDriveMode();

    ActivityInterface getActivityInterface();

}
