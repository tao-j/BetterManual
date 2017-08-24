package com.obsidium.bettermanual.capture;

import android.util.Pair;

import com.obsidium.bettermanual.ActivityInterface;
import com.obsidium.bettermanual.CameraUtil;
import com.obsidium.bettermanual.ManualActivity;
import com.obsidium.bettermanual.views.ExposureModeView;
import com.sony.scalar.hardware.CameraEx;

public class CaptureModeBracket extends CaptureMode implements  CameraEx.ShutterSpeedChangeListener {

    // Bracketing
    private int             m_bracketStep;  // in 1/3 stops
    private int             m_bracketMaxPicCount;
    private int             m_bracketPicCount;
    private int             m_bracketShutterDelta;
    private Pair<Integer, Integer> m_bracketNextShutterSpeed;
    private int             m_bracketNeutralShutterIndex;

    public CaptureModeBracket(ActivityInterface activityInterface)
    {
        super(activityInterface);
    }

    @Override
    public void reset() {
        calcMaxBracketPicCount();
        updateBracketPicCount();
    }

    @Override
    public void prepare() {
        if (activityInterface.getDialMode() == ManualActivity.DialMode.bracketSetStep || activityInterface.getDialMode() == ManualActivity.DialMode.bracketSetPicCount)
            abort();
        else
        {
            if (activityInterface.getExposureMode().get() != ExposureModeView.ExposureModes.manual)
            {
                activityInterface.showMessageDelayed("Scene mode must be set to manual");
                return;
            }
            if (activityInterface.getIso().getCurrentIso() == 0)
            {
                activityInterface.showMessageDelayed("ISO must be set to manual");
                return;
            }

            activityInterface.setLeftViewVisibility(false);

            activityInterface.setDialMode(ManualActivity.DialMode.bracketSetStep);
            m_bracketPicCount = 3;
            m_bracketStep = 3;
            m_bracketShutterDelta = 0;
            updateBracketStep();

            // Remember current shutter speed
            m_bracketNeutralShutterIndex = CameraUtil.getShutterValueIndex(activityInterface.getShutter().getCurrentShutterSpeed());
        }
    }

    @Override
    public void startShooting() {
        activityInterface.hideHintMessage();
        activityInterface.hideMessage();
        // Take first picture at set shutter speed
        activityInterface.takePicture();
    }

    @Override
    public void abort() {
        activityInterface.getMainHandler().removeCallbacks(m_countDownRunnable);
        //m_handler.removeCallbacks(m_timelapseRunnable);
        isActive = false;
        activityInterface.showMessageDelayed("Bracketing finished");
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

        // Reset to previous shutter speed
        final int shutterDiff = m_bracketNeutralShutterIndex - CameraUtil.getShutterValueIndex(activityInterface.getShutter().getCurrentShutterSpeed());
        if (shutterDiff != 0)
            activityInterface.getCamera().adjustShutterSpeed(-shutterDiff);
    }

    @Override
    public void onShutter(int i) {
        if (i == 0)
        {
            if (--m_bracketPicCount == 0)
                abort();
            else
            {
                m_bracketShutterDelta += m_bracketStep;
                final int shutterIndex = CameraUtil.getShutterValueIndex(activityInterface.getShutter().getCurrentShutterSpeed());
                if (m_bracketShutterDelta % 2 == 0)
                {
                    // Even, reduce shutter speed
                    m_bracketNextShutterSpeed = new Pair<Integer, Integer>(CameraUtil.SHUTTER_SPEEDS[shutterIndex + m_bracketShutterDelta][0],
                            CameraUtil.SHUTTER_SPEEDS[shutterIndex + m_bracketShutterDelta][1]);
                    activityInterface.getCamera().adjustShutterSpeed(-m_bracketShutterDelta);
                }
                else
                {
                    // Odd, increase shutter speed
                    m_bracketNextShutterSpeed = new Pair<Integer, Integer>(CameraUtil.SHUTTER_SPEEDS[shutterIndex - m_bracketShutterDelta][0],
                            CameraUtil.SHUTTER_SPEEDS[shutterIndex - m_bracketShutterDelta][1]);
                    activityInterface.getCamera().adjustShutterSpeed(m_bracketShutterDelta);
                }
            }
        }
        else
        {
            abort();
        }
    }

    @Override
    public void increment() {
        if (m_bracketStep < 9)
        {
            ++m_bracketStep;
            updateBracketStep();
        }
    }

    @Override
    public void decrement() {
        if (m_bracketStep > 1)
        {
            --m_bracketStep;
            updateBracketStep();
        }
    }

    public void decrementPicCount()
    {
        if (m_bracketPicCount > 3)
        {
            m_bracketPicCount -= 2;
            updateBracketPicCount();
        }
    }

    public void incrementPicCount()
    {
        if (m_bracketPicCount < m_bracketMaxPicCount)
        {
            m_bracketPicCount += 2;
            updateBracketPicCount();
        }
    }

    protected void calcMaxBracketPicCount()
    {
        final int index = CameraUtil.getShutterValueIndex(activityInterface.getShutter().getCurrentShutterSpeed());
        final int maxSteps = Math.min(index, CameraUtil.SHUTTER_SPEEDS.length - 1 - index);
        m_bracketMaxPicCount = (maxSteps / m_bracketStep) * 2 + 1;
    }

    protected void updateBracketStep()
    {

        final int mod = m_bracketStep % 3;
        final int ev;
        if (mod == 0)
            ev = 0;
        else if (mod == 1)
            ev = 3;
        else
            ev = 7;
        activityInterface.showMessage(String.format("%d.%dEV", m_bracketStep / 3, ev));
    }

    protected void updateBracketPicCount()
    {
        activityInterface.showMessage(String.format("%d pictures", m_bracketPicCount));
    }


    @Override
    public void onShutterSpeedChange(CameraEx.ShutterSpeedInfo shutterSpeedInfo, CameraEx cameraEx)
    {
        activityInterface.getShutter().updateShutterSpeed(shutterSpeedInfo.currentShutterSpeed_n, shutterSpeedInfo.currentShutterSpeed_d);
        if (m_bracketNextShutterSpeed != null)
        {

            if (shutterSpeedInfo.currentShutterSpeed_n == m_bracketNextShutterSpeed.first &&
                    shutterSpeedInfo.currentShutterSpeed_d == m_bracketNextShutterSpeed.second)
            {
                // Focus speed adjusted, take next picture
                activityInterface.takePicture();
            }
        }
    }
}
