package com.obsidium.bettermanual.capture;

import android.util.Pair;

import com.github.killerink.KeyEvents;
import com.obsidium.bettermanual.CameraUiInterface;
import com.obsidium.bettermanual.CameraUtil;
import com.obsidium.bettermanual.views.ExposureModeView;
import com.sony.scalar.hardware.CameraEx;

public class CaptureModeBracket extends CaptureMode implements  CameraEx.ShutterSpeedChangeListener, KeyEvents {

    // Bracketing
    private int             m_bracketStep;  // in 1/3 stops
    private int             m_bracketMaxPicCount;
    private int             m_bracketPicCount;
    private int             m_bracketShutterDelta;
    private Pair<Integer, Integer> m_bracketNextShutterSpeed;
    private int             m_bracketNeutralShutterIndex;

    private final int BRACKET_NON = 0;
    private final int BRACKET_STEP = 1;
    private final int BRACKET_PICCOUNT = 2;
    private int currentDialMode = BRACKET_NON;


    public CaptureModeBracket(CameraUiInterface cameraUiInterface)
    {
        super(cameraUiInterface);
    }

    @Override
    public void reset() {
        calcMaxBracketPicCount();
        updateBracketPicCount();
    }

    @Override
    public void prepare() {
        if (isActive())
            abort();
        else
        {
            if (cameraUiInterface.getExposureMode().get() != ExposureModeView.ExposureModes.manual)
            {
                cameraUiInterface.showMessageDelayed("Scene mode must be set to manual");
                return;
            }
            if (cameraUiInterface.getIso().getCurrentIso() == 0)
            {
                cameraUiInterface.showMessageDelayed("ISO must be set to manual");
                return;
            }

            cameraUiInterface.setLeftViewVisibility(false);

            m_bracketPicCount = 3;
            m_bracketStep = 3;
            m_bracketShutterDelta = 0;
            updateBracketStep();

            // Remember current shutter speed
            m_bracketNeutralShutterIndex = CameraUtil.getShutterValueIndex(cameraUiInterface.getActivityInterface().getCamera().getShutterSpeed());
        }
    }

    @Override
    public void startShooting() {
        cameraUiInterface.hideHintMessage();
        cameraUiInterface.hideMessage();
        // Take first picture at set shutter speed
        cameraUiInterface.getActivityInterface().getCamera().takePicture();
    }

    @Override
    public void abort() {
        cameraUiInterface.getActivityInterface().getMainHandler().removeCallbacks(m_countDownRunnable);
        //m_handler.removeCallbacks(m_timelapseRunnable);
        isActive = false;
        cameraUiInterface.showMessageDelayed("Bracketing finished");
        cameraUiInterface.getActivityInterface().getCamera().startPreview();
        cameraUiInterface.getActivityInterface().getCamera().startDisplay();

        // Update controls
        cameraUiInterface.hideHintMessage();
        cameraUiInterface.setLeftViewVisibility(true);
        cameraUiInterface.getExposureMode().updateImage();
        cameraUiInterface.getDriveMode().updateImage();

        cameraUiInterface.setActiveViewFlag(cameraUiInterface.getActivityInterface().getPreferences().getViewFlags(cameraUiInterface.getActiveViewsFlag()));
        cameraUiInterface.updateViewVisibility();

        // Reset to previous shutter speed
        final int shutterDiff = m_bracketNeutralShutterIndex - CameraUtil.getShutterValueIndex(cameraUiInterface.getActivityInterface().getCamera().getShutterSpeed());
        if (shutterDiff != 0)
            cameraUiInterface.getActivityInterface().getCamera().adjustShutterSpeed(-shutterDiff);
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
                final int shutterIndex = CameraUtil.getShutterValueIndex(cameraUiInterface.getActivityInterface().getCamera().getShutterSpeed());
                if (m_bracketShutterDelta % 2 == 0)
                {
                    // Even, reduce shutter speed
                    m_bracketNextShutterSpeed = new Pair<Integer, Integer>(CameraUtil.SHUTTER_SPEEDS[shutterIndex + m_bracketShutterDelta][0],
                            CameraUtil.SHUTTER_SPEEDS[shutterIndex + m_bracketShutterDelta][1]);
                    cameraUiInterface.getActivityInterface().getCamera().adjustShutterSpeed(-m_bracketShutterDelta);
                }
                else
                {
                    // Odd, increase shutter speed
                    m_bracketNextShutterSpeed = new Pair<Integer, Integer>(CameraUtil.SHUTTER_SPEEDS[shutterIndex - m_bracketShutterDelta][0],
                            CameraUtil.SHUTTER_SPEEDS[shutterIndex - m_bracketShutterDelta][1]);
                    cameraUiInterface.getActivityInterface().getCamera().adjustShutterSpeed(m_bracketShutterDelta);
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
        final int index = CameraUtil.getShutterValueIndex(cameraUiInterface.getActivityInterface().getCamera().getShutterSpeed());
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
        cameraUiInterface.showMessage(String.format("%d.%dEV", m_bracketStep / 3, ev));
    }

    protected void updateBracketPicCount()
    {
        cameraUiInterface.showMessage(String.format("%d pictures", m_bracketPicCount));
    }


    @Override
    public void onShutterSpeedChange(CameraEx.ShutterSpeedInfo shutterSpeedInfo, CameraEx cameraEx)
    {
        cameraUiInterface.getShutter().updateShutterSpeed(shutterSpeedInfo.currentShutterSpeed_n, shutterSpeedInfo.currentShutterSpeed_d);
        if (m_bracketNextShutterSpeed != null)
        {

            if (shutterSpeedInfo.currentShutterSpeed_n == m_bracketNextShutterSpeed.first &&
                    shutterSpeedInfo.currentShutterSpeed_d == m_bracketNextShutterSpeed.second)
            {
                // Focus speed adjusted, take next picture
                cameraUiInterface.getActivityInterface().getCamera().takePicture();
            }
        }
    }

    @Override
    public boolean onUpperDialChanged(int value) {
        if (currentDialMode == BRACKET_STEP)
        {
            if (value <0)
                decrement();
            else
                increment();
        }
        else
        if (currentDialMode == BRACKET_PICCOUNT)
        {
            if (value < 0)
                decrementPicCount();
            else
                incrementPicCount();
        }
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


        return false;
    }

    @Override
    public boolean onEnterKeyUp() {
        if (currentDialMode == BRACKET_NON)
        {
            prepare();
            currentDialMode = BRACKET_STEP;
            updateBracketStep();
        }
        else if(currentDialMode == BRACKET_STEP)
        {
            cameraUiInterface.showHintMessage("\uE4CD to set picture count, \uE04C to confirm");
            currentDialMode = BRACKET_PICCOUNT;
            reset();
            updateBracketPicCount();
        }
        else
        {
            cameraUiInterface.getActivityInterface().getDialHandler().setDialEventListner((KeyEvents)cameraUiInterface);
            startCountDown();
            currentDialMode = BRACKET_NON;
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
}
