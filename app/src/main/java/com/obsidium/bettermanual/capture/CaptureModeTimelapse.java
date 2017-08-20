package com.obsidium.bettermanual.capture;

import com.obsidium.bettermanual.ActivityInterface;
import com.obsidium.bettermanual.ManualActivity;
import com.sony.scalar.sysutil.didep.Settings;

public class CaptureModeTimelapse extends CaptureMode
{

    private int             m_timelapseInterval;    // ms
    private int             m_timelapsePicCount;
    private int             m_timelapsePicsTaken;
    private int             m_autoPowerOffTimeBackup;

    public CaptureModeTimelapse(ActivityInterface manualActivity)
    {
        super(manualActivity);
    }

    public void reset()
    {
        m_timelapsePicCount = 0;
        updateTimelapsePictureCount();
    }

    @Override
    public void prepare() {
        if (activityInterface.getDialMode() == ManualActivity.DialMode.timelapseSetInterval || activityInterface.getDialMode() == ManualActivity.DialMode.timelapseSetPicCount)
            abort();
        else
        {
            activityInterface.setLeftViewVisibility(false);

            activityInterface.setDialMode(ManualActivity.DialMode.timelapseSetInterval);
            m_timelapseInterval = 1000;
            updateTimelapseInterval();
            activityInterface.showHintMessage("\uE4CD to set timelapse interval, \uE04C to confirm");


            // Not supported on some camera models
            try
            {
                m_autoPowerOffTimeBackup = Settings.getAutoPowerOffTime();
            }
            catch (NoSuchMethodError e)
            {
            }
        }
    }

    @Override
    public void startShooting() {
        activityInterface.hideHintMessage();
        activityInterface.hideMessage();
        try
        {
            Settings.setAutoPowerOffTime(m_timelapseInterval / 1000 * 2);
        }
        catch (NoSuchMethodError e)
        {
        }
        activityInterface.getMainHandler().post(m_timelapseRunnable);
    }

    @Override
    public void abort() {
        activityInterface.getMainHandler().removeCallbacks(m_countDownRunnable);
        activityInterface.getMainHandler().removeCallbacks(m_timelapseRunnable);
        isActive = false;
        activityInterface.showMessageDelayed("Timelapse finished");
        activityInterface.setDialMode(ManualActivity.DialMode.shutter);
        activityInterface.getCamera().startDirectShutter();
        activityInterface.getCamera().getNormalCamera().startPreview();

            // Update controls
        activityInterface.hideHintMessage();
        activityInterface.setLeftViewVisibility(true);
        activityInterface.getExposureMode().updateImage();
        activityInterface.getDriveMode().updateImage();

        activityInterface.setActiveViewFlag(activityInterface.getPreferences().getViewFlags(activityInterface.getActiveViewsFlag()));
        activityInterface.updateViewVisibility();

            try
            {
                Settings.setAutoPowerOffTime(m_autoPowerOffTimeBackup);
            }
            catch (NoSuchMethodError e)
            {
            }

    }

    @Override
    public void onShutter(int i) {
        if (i == 0)
        {
            ++m_timelapsePicsTaken;
            if (m_timelapsePicCount < 0 || m_timelapsePicCount == 1)
                abort();
            else
            {
                if (m_timelapsePicCount != 0)
                    --m_timelapsePicCount;
                if (m_timelapseInterval >= 1000)
                {
                    if (m_timelapsePicCount > 0)
                        activityInterface.showMessageDelayed(String.format("%d pictures remaining", m_timelapsePicCount));
                    else
                        activityInterface.showMessageDelayed(String.format("%d pictures taken", m_timelapsePicsTaken));
                }
                if (m_timelapseInterval != 0)
                    activityInterface.getMainHandler().postDelayed(m_timelapseRunnable, m_timelapseInterval);
                else
                    activityInterface.getCamera().burstableTakePicture();
            }
        }
        else
        {
            abort();
        }
    }

    @Override
    public void decrement()
    {
        if (m_timelapseInterval > 0)
        {
            if (m_timelapseInterval <= 1000)
                m_timelapseInterval -= 100;
            else
                m_timelapseInterval -= 1000;
        }
        updateTimelapseInterval();
    }

    @Override
    public void increment()
    {
        if (m_timelapseInterval < 1000)
            m_timelapseInterval += 100;
        else
            m_timelapseInterval += 1000;
        updateTimelapseInterval();
    }

    protected void updateTimelapseInterval()
    {
        if (m_timelapseInterval == 0)
            activityInterface.showMessage("No delay");
        else if (m_timelapseInterval < 1000)
            activityInterface.showMessage(String.format("%d msec", m_timelapseInterval));
        else if (m_timelapseInterval == 1000)
            activityInterface.showMessage("1 second");
        else
            activityInterface.showMessage(String.format("%d seconds", m_timelapseInterval / 1000));
    }

    public void updateTimelapsePictureCount()
    {
        if (m_timelapsePicCount == 0)
            activityInterface.showMessage("No picture limit");
        else
            activityInterface.showMessage(String.format("%d pictures", m_timelapsePicCount));
    }

    public void decrementPicCount()
    {
        if (m_timelapsePicCount > 0)
            --m_timelapsePicCount;
        updateTimelapsePictureCount();
    }

    public void incrementPicCount()
    {
        ++m_timelapsePicCount;
        updateTimelapsePictureCount();
    }

    protected final Runnable  m_timelapseRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            activityInterface.getCamera().burstableTakePicture();
        }
    };
}
