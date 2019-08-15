package com.obsidium.bettermanual.capture;

import android.view.View;

import com.obsidium.bettermanual.Preferences;
import com.obsidium.bettermanual.R;
import com.obsidium.bettermanual.camera.CameraInstance;
import com.obsidium.bettermanual.controller.ImageViewController;
import com.obsidium.bettermanual.layout.CameraUiInterface;
import com.obsidium.bettermanual.model.Model;


public abstract class CaptureMode extends ImageViewController implements CaptureModeInterface
{
    public static final int COUNTDOWN_SECONDS = 5;

    protected CameraUiInterface cameraUiInterface;
    protected boolean isActive = false;


    protected int             m_countdown;

    public CaptureMode(CameraUiInterface cameraUiInterface)
    {
        this.cameraUiInterface = cameraUiInterface;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public int getNavigationHelpID() {
        return 0;
    }

    protected final Runnable  m_countDownRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            if (--m_countdown > 0)
            {
                cameraUiInterface.showMessage(String.format("Starting in %d...", m_countdown));
                cameraUiInterface.getActivityInterface().getMainHandler().postDelayed(this, 1000);
            }
            else
            {
                cameraUiInterface.hideMessage();
                startShooting();
            }
        }
    };

    @Override
    public void startCountDown() {
        isActive = true;
        //cameraUiInterface.getActivityInterface().getCamera().disableHwShutterButton();
        cameraUiInterface.showHintMessage(cameraUiInterface.getActivityInterface().getResString(R.string.icon_enterButton) + " to abort");
        // Stop preview (doesn't seem to preserve battery life?)
        CameraInstance.GET().stopPreview();

        // Hide some bottom views
        Preferences.GET().setViewFlags(cameraUiInterface.getActiveViewsFlag());
        cameraUiInterface.setActiveViewFlag(0);
        cameraUiInterface.updateViewVisibility();

        // Start countdown
        m_countdown = COUNTDOWN_SECONDS;
        cameraUiInterface.showMessage(String.format("Starting in %d...", m_countdown));
        cameraUiInterface.getActivityInterface().getMainHandler().postDelayed(m_countDownRunnable, 1000);
    }

    @Override
    protected void updateImage() {

    }

    @Override
    public void onIsSupportedChanged() {
        if(view != null)
            view.setVisibility(View.VISIBLE);
    }
}
