package com.obsidium.bettermanual.capture;

import android.util.Log;

import com.github.killerink.KeyEvents;
import com.github.killerink.camera.CaptureSession;
import com.obsidium.bettermanual.CameraUiInterface;
import com.obsidium.bettermanual.R;
import com.sony.scalar.sysutil.didep.Settings;

public class CaptureModeTimelapse extends CaptureMode implements KeyEvents, CaptureSession.CaptureDoneEvent
{

    private final String TAG = CaptureModeTimelapse.class.getSimpleName();

    private int             m_timelapseInterval;    // ms
    private int             m_timelapsePicCount;
    private int             m_timelapsePicsTaken;
    private int             m_autoPowerOffTimeBackup;

    private final int TLS_SET_NONE = 0;
    private final int TLS_SET_INTERVAL = 1;
    private final int TLS_SET_PICCOUNT = 2;
    private int currentdial = TLS_SET_NONE;
    private CaptureSession captureSession;


    public CaptureModeTimelapse(CameraUiInterface manualActivity)
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
            cameraUiInterface.setLeftViewVisibility(false);

            currentdial = TLS_SET_INTERVAL;
            //cameraUiInterface.setDialMode(CameraUiFragment.DialMode.timelapseSetInterval);
            m_timelapseInterval = 1000;
            updateTimelapseInterval();
            cameraUiInterface.showHintMessage(cameraUiInterface.getActivityInterface().getResString(R.string.icon_lowerDial) +
                    " to set timelapse interval, "
                    +cameraUiInterface.getActivityInterface().getResString(R.string.icon_enterButton)
                    + " to confirm");


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
        Log.d(TAG,"startShooting");
        cameraUiInterface.hideHintMessage();
        cameraUiInterface.hideMessage();
        try
        {
            Settings.setAutoPowerOffTime(m_timelapseInterval / 1000 * 2);
        }
        catch (NoSuchMethodError e)
        {
        }
        captureSession = new CaptureSession(cameraUiInterface.getActivityInterface().getCamera(),false,this);
        cameraUiInterface.getActivityInterface().getBackHandler().post(captureSession);
    }

    @Override
    public void abort() {
        cameraUiInterface.getActivityInterface().getMainHandler().removeCallbacks(m_countDownRunnable);
        cameraUiInterface.getActivityInterface().getMainHandler().removeCallbacks(m_timelapseRunnable);
        isActive = false;
        cameraUiInterface.showMessageDelayed("Timelapse finished");
        cameraUiInterface.getActivityInterface().getCamera().startPreview();
        cameraUiInterface.getActivityInterface().getCamera().startDisplay();

            // Update controls
        cameraUiInterface.getActivityInterface().getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                cameraUiInterface.hideHintMessage();
                cameraUiInterface.setLeftViewVisibility(true);
                cameraUiInterface.getExposureMode().updateImage();
                cameraUiInterface.getDriveMode().updateImage();

                cameraUiInterface.setActiveViewFlag(cameraUiInterface.getActivityInterface().getPreferences().getViewFlags(cameraUiInterface.getActiveViewsFlag()));
                cameraUiInterface.updateViewVisibility();
            }
        });


            try
            {
                Settings.setAutoPowerOffTime(m_autoPowerOffTimeBackup);
            }
            catch (NoSuchMethodError e)
            {
            }
        currentdial = TLS_SET_NONE;

    }


    /*@Override
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
                        cameraUiInterface.showMessageDelayed(String.format("%d pictures remaining", m_timelapsePicCount));
                    else
                        cameraUiInterface.showMessageDelayed(String.format("%d pictures taken", m_timelapsePicsTaken));
                }
                if (m_timelapseInterval != 0)
                    cameraUiInterface.getActivityInterface().getMainHandler().postDelayed(m_timelapseRunnable, m_timelapseInterval);
                else
                    cameraUiInterface.getActivityInterface().getCamera().takePicture();
            }
        }
        else
        {
            abort();
        }
    }*/

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

    private void updateTimelapseInterval()
    {
        if (m_timelapseInterval == 0)
            cameraUiInterface.showMessage("No delay");
        else if (m_timelapseInterval < 1000)
            cameraUiInterface.showMessage(String.format("%d msec", m_timelapseInterval));
        else if (m_timelapseInterval == 1000)
            cameraUiInterface.showMessage("1 second");
        else
            cameraUiInterface.showMessage(String.format("%d seconds", m_timelapseInterval / 1000));
    }

    private void updateTimelapsePictureCount()
    {
        if (m_timelapsePicCount == 0)
            cameraUiInterface.showMessage("No picture limit");
        else
            cameraUiInterface.showMessage(String.format("%d pictures", m_timelapsePicCount));
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

    private final Runnable  m_timelapseRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            startShooting();
        }
    };

    @Override
    public boolean onUpperDialChanged(int value) {
        return false;
    }

    @Override
    public boolean onLowerDialChanged(int value) {
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

        return false;
    }

    @Override
    public boolean onEnterKeyUp() {
        Log.d(TAG,"onEnterKeyDown" + currentdial);
        if (currentdial == TLS_SET_NONE) {
            prepare();
            updateTimelapseInterval();
        }
        else if (currentdial == TLS_SET_INTERVAL) {
            cameraUiInterface.showHintMessage(cameraUiInterface.getActivityInterface().getResString(R.string.icon_lowerDial)
                    + " to set picture count,"
                    + cameraUiInterface.getActivityInterface().getResString(R.string.icon_enterButton)
                    + " to confirm");
            currentdial = TLS_SET_PICCOUNT;
            updateTimelapsePictureCount();
        }
        else if (currentdial == TLS_SET_PICCOUNT)
        {
            Log.d(TAG, "onEnterKeyDown setDefaultDialListner");
            cameraUiInterface.getActivityInterface().getDialHandler().setDialEventListner((KeyEvents)cameraUiInterface);
            Log.d(TAG, "onEnterKeyDown startCountDown");
            startCountDown();
            currentdial = TLS_SET_NONE;
        }
        return false;
    }

    @Override
    public boolean onFnKeyDown() {
        return false;
    }

    @Override
    public boolean onFnKeyUp() {
        return false;
    }

    @Override
    public boolean onAelKeyDown() {
        return false;
    }

    @Override
    public boolean onAelKeyUp() {
        return false;
    }

    @Override
    public boolean onMenuKeyDown() {
        return false;
    }

    @Override
    public boolean onMenuKeyUp() {
        return false;
    }

    @Override
    public boolean onFocusKeyDown() {
        return false;
    }

    @Override
    public boolean onFocusKeyUp() {
        return false;
    }

    @Override
    public boolean onShutterKeyDown() {
        return false;
    }

    @Override
    public boolean onShutterKeyUp() {
        return false;
    }

    @Override
    public boolean onPlayKeyDown() {
        return false;
    }

    @Override
    public boolean onPlayKeyUp() {
        return false;
    }

    @Override
    public boolean onMovieKeyDown() {
        return false;
    }

    @Override
    public boolean onMovieKeyUp() {
        return false;
    }

    @Override
    public boolean onC1KeyDown() {
        return false;
    }

    @Override
    public boolean onC1KeyUp() {
        return false;
    }

    @Override
    public boolean onLensAttached() {
        return false;
    }

    @Override
    public boolean onLensDetached() {
        return false;
    }

    @Override
    public boolean onModeDialChanged(int value) {
        return false;
    }

    @Override
    public boolean onZoomTeleKey() {
        return false;
    }

    @Override
    public boolean onZoomWideKey() {
        return false;
    }

    @Override
    public boolean onZoomOffKey() {
        return false;
    }

    @Override
    public boolean onDeleteKeyDown() {
        return false;
    }

    @Override
    public boolean onDeleteKeyUp() {
        return false;
    }

    @Override
    public void onCaptureDone() {
        Log.d(TAG,"onCaptureDone");
        ++m_timelapsePicsTaken;
        if (m_timelapsePicCount < 0 || m_timelapsePicCount == 1) {
            abort();
            cameraUiInterface.onCaptureDone();
            captureSession = null;
            Log.d(TAG, "abort Timelapse");
        }
        else
        {
            if (m_timelapsePicCount != 0)
                --m_timelapsePicCount;
            if (m_timelapseInterval >= 1000)
            {
                if (m_timelapsePicCount > 0)
                    cameraUiInterface.showMessageDelayed(String.format("%d pictures remaining", m_timelapsePicCount));
                else
                    cameraUiInterface.showMessageDelayed(String.format("%d pictures taken", m_timelapsePicsTaken));
            }
            if (m_timelapseInterval != 0) {
                Log.d(TAG,"next Capture in " + m_timelapseInterval);
                cameraUiInterface.getActivityInterface().getMainHandler().postDelayed(m_timelapseRunnable, m_timelapseInterval);
            }
            else
                startShooting();
        }
    }
}
