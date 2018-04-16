package com.obsidium.bettermanual.layout;

import com.obsidium.bettermanual.ActivityInterface;

public interface CameraUiInterface
{
    void showMessageDelayed(String msg);
    void showMessage(String msg);
    void hideMessage();
    void showHintMessage(String msg);
    void hideHintMessage();
    int getActiveViewsFlag();
    void setActiveViewFlag(int viewsToShow);


    void updateViewVisibility();
    void setLeftViewVisibility(boolean state);

    ActivityInterface getActivityInterface();

}
