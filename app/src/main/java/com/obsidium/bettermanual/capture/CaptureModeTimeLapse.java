package com.obsidium.bettermanual.capture;

import android.util.Log;

import com.obsidium.bettermanual.KeyEvents;
import com.obsidium.bettermanual.Preferences;
import com.obsidium.bettermanual.R;
import com.obsidium.bettermanual.camera.CameraInstance;
import com.obsidium.bettermanual.camera.CaptureSession;
import com.obsidium.bettermanual.controller.DriveModeController;
import com.obsidium.bettermanual.controller.ExposureModeController;
import com.obsidium.bettermanual.layout.CameraUIInterface;
import com.sony.scalar.sysutil.didep.Settings;

public class CaptureModeTimeLapse extends CaptureMode implements KeyEvents, CaptureSession.CaptureDoneEvent {

    private final String TAG = CaptureModeTimeLapse.class.getSimpleName();
    private final int TLS_STOPPED = 0;
    private final int TLS_SET_INTERVAL = 1;
    private final int TLS_SET_PIC_COUNT = 2;
    private final int TLS_RUNNING = 3;
    private int m_currentState = TLS_STOPPED;
    private int m_timeLapseInterval;    // ms
    private int m_timeLapsePicCount;
    private int m_timeLapsePicsTaken;
    private int m_autoPowerOffTimeBackup;

    private final Runnable m_timeLapseRunnable = () -> startShooting();

    public CaptureModeTimeLapse(CameraUIInterface manualActivity) {
        super(manualActivity);
    }

    @Override
    public void toggle() {
        if (isActive()) {
            abort();
        } else {
            cameraUIInterface.getActivityInterface().getDialHandler().setDialEventListener(this);
            onEnterKeyUp();
        }
    }

    @Override
    public int getNavigationHelpID() {
        return R.string.view_Timelapse;
    }

    public void reset() {
        m_timeLapsePicCount = 0;
        m_currentState = TLS_SET_INTERVAL;
        updateTimeLapsePictureCount();
    }

    @Override
    public boolean prepare() {
        return false;
    }

    @Override
    public void startShooting() {
        Log.d(TAG, "startShooting");
        cameraUIInterface.hideHintMessage();
        cameraUIInterface.hideMessage();
        try {
            Settings.setAutoPowerOffTime(m_timeLapseInterval / 1000 * 2);
        }
        catch (NoSuchMethodError ignored) {
        }
        cameraUIInterface.getActivityInterface().setBulbCapture(false);
        cameraUIInterface.getActivityInterface().setCaptureDoneEventListner(this);
        CameraInstance.GET().takePicture();
    }

    @Override
    public void abort() {
        cameraUIInterface.getActivityInterface().getMainHandler().removeCallbacks(m_countDownRunnable);
        cameraUIInterface.getActivityInterface().getMainHandler().removeCallbacks(m_timeLapseRunnable);
        isActive = false;
        cameraUIInterface.showMessageDelayed("Timelapse finished");
        CameraInstance.GET().enableHwShutterButton();
        CameraInstance.GET().startPreview();

        // Update controls
        cameraUIInterface.getActivityInterface().getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                cameraUIInterface.hideHintMessage();
                cameraUIInterface.setLeftViewVisibility(true);
                ExposureModeController.GetInstance().onValueChanged();
                DriveModeController.GetInstance().onValueChanged();

                cameraUIInterface.setActiveViewFlag(Preferences.GET().getViewFlags(cameraUIInterface.getActiveViewsFlag()));
                cameraUIInterface.updateViewVisibility();
            }
        });


        try {
            Settings.setAutoPowerOffTime(m_autoPowerOffTimeBackup);
        }
        catch (NoSuchMethodError ignored) {
        }
        m_currentState = TLS_STOPPED;
    }

    @Override
    public void decrementInterval() {
        if (m_timeLapseInterval > 0) {
            if (m_timeLapseInterval <= 1000)
                m_timeLapseInterval -= 100;
            else
                m_timeLapseInterval -= 1000;
        }
        updateTimeLapseInterval();
    }

    @Override
    public void incrementInterval() {
        if (m_timeLapseInterval < 1000)
            m_timeLapseInterval += 100;
        else
            m_timeLapseInterval += 1000;
        updateTimeLapseInterval();
    }

    private void updateTimeLapseInterval() {
        if (m_timeLapseInterval == 0)
            cameraUIInterface.showMessage("No delay");
        else if (m_timeLapseInterval < 1000)
            cameraUIInterface.showMessage(String.format("%d msec", m_timeLapseInterval));
        else if (m_timeLapseInterval == 1000)
            cameraUIInterface.showMessage("1 second");
        else
            cameraUIInterface.showMessage(String.format("%d seconds", m_timeLapseInterval / 1000));
    }

    private void updateTimeLapsePictureCount() {
        if (m_timeLapsePicCount == 0)
            cameraUIInterface.showMessage("No picture limit");
        else
            cameraUIInterface.showMessage(String.format("%d pictures", m_timeLapsePicCount));
    }

    public void decrementPicCount() {
        if (m_timeLapsePicCount > 0)
            --m_timeLapsePicCount;
        updateTimeLapsePictureCount();
    }

    public void incrementPicCount() {
        ++m_timeLapsePicCount;
        updateTimeLapsePictureCount();
    }

    @Override
    public boolean onUpperDialChanged(int value) {
        return false;
    }

    @Override
    public boolean onLowerDialChanged(int value) {
        if (m_currentState == TLS_SET_INTERVAL) {
            if (value < 0)
                decrementInterval();
            else
                incrementInterval();
        }
        else if (m_currentState == TLS_SET_PIC_COUNT) {
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
        Log.d(TAG, "onEnterKeyDown" + m_currentState);
        if (m_currentState == TLS_STOPPED) {
            if (isActive()) {
                abort();
            }
            else {
                cameraUIInterface.setLeftViewVisibility(false);
                m_currentState = TLS_SET_INTERVAL;
                m_timeLapseInterval = 3000;
                cameraUIInterface.showHintMessage(cameraUIInterface.getActivityInterface().getResString(R.string.icon_lowerDial) +
                        " to set time-lapse interval, "
                        + cameraUIInterface.getActivityInterface().getResString(R.string.icon_enterButton)
                        + " to confirm");

                // Not supported on some camera models
                try {
                    m_autoPowerOffTimeBackup = Settings.getAutoPowerOffTime();
                }
                catch (NoSuchMethodError ignored) {
                }
            }
//            updateTimeLapseInterval();
        }
        else if (m_currentState == TLS_SET_INTERVAL) {
            cameraUIInterface.showHintMessage(cameraUIInterface.getActivityInterface().getResString(R.string.icon_lowerDial)
                    + " to set picture count,"
                    + cameraUIInterface.getActivityInterface().getResString(R.string.icon_enterButton)
                    + " to confirm");
            m_currentState = TLS_SET_PIC_COUNT;
            updateTimeLapsePictureCount();
        }
        else if (m_currentState == TLS_SET_PIC_COUNT) {
            Log.d(TAG, "onEnterKeyDown setDefaultDialListner");
            cameraUIInterface.getActivityInterface().getDialHandler().setDialEventListener((KeyEvents) cameraUIInterface);
            Log.d(TAG, "onEnterKeyDown startCountDown");
            startCountDown();
            m_currentState = TLS_STOPPED;
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
        Log.d(TAG, "onCaptureDone");
        ++m_timeLapsePicsTaken;
        if (m_timeLapsePicCount < 0 || m_timeLapsePicCount == 1) {
            abort();
            cameraUIInterface.getActivityInterface().setCaptureDoneEventListner(null);
            Log.d(TAG, "abort Timelapse");
        } else {
            if (m_timeLapsePicCount != 0)
                --m_timeLapsePicCount;
            if (m_timeLapseInterval >= 1000) {
                if (m_timeLapsePicCount > 0)
                    cameraUIInterface.showMessageDelayed(String.format("%d pictures remaining", m_timeLapsePicCount));
                else
                    cameraUIInterface.showMessageDelayed(String.format("%d pictures taken", m_timeLapsePicsTaken));
            }
            if (m_timeLapseInterval != 0) {
                Log.d(TAG, "next Capture in " + m_timeLapseInterval);
                cameraUIInterface.getActivityInterface().getMainHandler().postDelayed(m_timeLapseRunnable, m_timeLapseInterval);
            } else
                startShooting();
        }
    }
}
