package com.obsidium.bettermanual.capture;

import android.view.View;

import com.obsidium.bettermanual.Preferences;
import com.obsidium.bettermanual.R;
import com.obsidium.bettermanual.camera.CameraInstance;
import com.obsidium.bettermanual.controller.ImageViewController;
import com.obsidium.bettermanual.layout.CameraUIInterface;


public abstract class CaptureMode extends ImageViewController implements CaptureModeInterface
{
    public static final int COUNTDOWN_SECONDS = 5;

    protected CameraUIInterface cameraUIInterface;
    protected boolean isActive = false;


    protected int             m_countdown;

    public CaptureMode(CameraUIInterface cameraUIInterface)
    {
        this.cameraUIInterface = cameraUIInterface;
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
                cameraUIInterface.showMessage(String.format("Starting in %d...", m_countdown));
                cameraUIInterface.getActivityInterface().getMainHandler().postDelayed(this, 1000);
            }
            else
            {
                cameraUIInterface.hideMessage();
                startShooting();
            }
        }
    };

    @Override
    public void startCountDown() {
        isActive = true;
        //cameraUiInterface.getActivityInterface().getCamera().disableHwShutterButton();
        cameraUIInterface.showHintMessage(cameraUIInterface.getActivityInterface().getResString(R.string.icon_enterButton) + " to abort");
        // Stop preview (doesn't seem to preserve battery life?)
        CameraInstance.GET().stopPreview();

        // Hide some bottom views
        Preferences.GET().setViewFlags(cameraUIInterface.getActiveViewsFlag());
        cameraUIInterface.setActiveViewFlag(0);
        cameraUIInterface.updateViewVisibility();

        // Start countdown
        m_countdown = COUNTDOWN_SECONDS;
        cameraUIInterface.showMessage(String.format("Starting in %d...", m_countdown));
        cameraUIInterface.getActivityInterface().getMainHandler().postDelayed(m_countDownRunnable, 1000);
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
