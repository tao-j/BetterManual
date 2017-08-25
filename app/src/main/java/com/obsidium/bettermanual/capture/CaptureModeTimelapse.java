package com.obsidium.bettermanual.capture;

import com.github.ma1co.pmcademo.app.DialPadKeysEvents;
import com.obsidium.bettermanual.ActivityInterface;
import com.obsidium.bettermanual.ManualActivity;
import com.sony.scalar.sysutil.didep.Settings;

public class CaptureModeTimelapse extends CaptureMode implements DialPadKeysEvents
{

    private int             m_timelapseInterval;    // ms
    private int             m_timelapsePicCount;
    private int             m_timelapsePicsTaken;
    private int             m_autoPowerOffTimeBackup;

    private final int TLS_SET_NONE = 0;
    private final int TLS_SET_INTERVAL = 1;
    private final int TLS_SET_PICCOUNT = 2;
    private int currentdial = TLS_SET_INTERVAL;


    public CaptureModeTimelapse(ActivityInterface manualActivity)
    {
        super(manualActivity);
    }

    public void reset()
    {
        m_timelapsePicCount = 0;
        currentdial = TLS_SET_INTERVAL;
        updateTimelapsePictureCount();
    }

    @Override
    public void prepare() {
        if (isActive())
            abort();
        else
        {
            activityInterface.setLeftViewVisibility(false);

            currentdial = TLS_SET_INTERVAL;
            //activityInterface.setDialMode(ManualActivity.DialMode.timelapseSetInterval);
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
        activityInterface.takePicture();
    }

    @Override
    public void abort() {
        activityInterface.getMainHandler().removeCallbacks(m_countDownRunnable);
        activityInterface.getMainHandler().removeCallbacks(m_timelapseRunnable);
        isActive = false;
        activityInterface.showMessageDelayed("Timelapse finished");
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
        currentdial = TLS_SET_NONE;

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
                    activityInterface.takePicture();
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
            activityInterface.takePicture();
        }
    };

    @Override
    public boolean onUpperDialChanged(int value) {
        if (currentdial == TLS_SET_INTERVAL)
        {
            if (value <0)
                decrement();
            else
                increment();
        }
        else
            if (currentdial == TLS_SET_PICCOUNT)
            {
                if (value < 0)
                    decrementPicCount();
                else
                    incrementPicCount();
            }
        onUpperDialChanged(value);
        return false;
    }

    @Override
    public boolean onLowerDialChanged(int value) {
        return false;
    }

    @Override
    public boolean onUpKeyDown() {
        return false;
    }

    @Override
    public boolean onUpKeyUp() {
        return false;
    }

    @Override
    public boolean onDownKeyDown() {
        return false;
    }

    @Override
    public boolean onDownKeyUp() {
        return false;
    }

    @Override
    public boolean onLeftKeyDown() {
        return false;
    }

    @Override
    public boolean onLeftKeyUp() {
        return false;
    }

    @Override
    public boolean onRightKeyDown() {
        return false;
    }

    @Override
    public boolean onRightKeyUp() {
        return false;
    }

    @Override
    public boolean onEnterKeyDown() {
        if (currentdial == TLS_SET_NONE) {
            prepare();
            currentdial = TLS_SET_INTERVAL;
        }
        else if (currentdial == TLS_SET_INTERVAL) {
            activityInterface.showHintMessage("\uE4CD to set picture count, \uE04C to confirm");
            currentdial = TLS_SET_PICCOUNT;
        }
        else if (currentdial == TLS_SET_PICCOUNT)
        {
            activityInterface.getDialHandler().setDefaultListner();
            startCountDown();
            currentdial = TLS_SET_NONE;
        }
        return false;
    }

    @Override
    public boolean onEnterKeyUp() {
        return false;
    }
}
