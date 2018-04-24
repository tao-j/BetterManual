package com.obsidium.bettermanual.capture;

import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.obsidium.bettermanual.CameraUtil;
import com.obsidium.bettermanual.KeyEvents;
import com.obsidium.bettermanual.Preferences;
import com.obsidium.bettermanual.R;
import com.obsidium.bettermanual.camera.CameraInstance;
import com.obsidium.bettermanual.camera.CaptureSession;
import com.obsidium.bettermanual.camera.ShutterSpeedValue;
import com.obsidium.bettermanual.controller.DriveModeController;
import com.obsidium.bettermanual.controller.ExposureModeController;
import com.obsidium.bettermanual.controller.IsoController;
import com.obsidium.bettermanual.controller.ShutterController;
import com.obsidium.bettermanual.layout.CameraUiInterface;
import com.obsidium.bettermanual.model.ExposureModeModel;
import com.obsidium.bettermanual.model.Model;
import com.sony.scalar.hardware.CameraEx;

public class CaptureModeBracket extends CaptureMode implements  ShutterController.ShutterSpeedEvent, KeyEvents, CaptureSession.CaptureDoneEvent {

    private final String TAG = CaptureModeBracket.class.getSimpleName();
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
    private final int MAX_RETRYS = 3;
    private int retryCount = 0;


    public CaptureModeBracket(CameraUiInterface cameraUiInterface)
    {
        super(cameraUiInterface);
    }

    @Override
    public void toggle() {
        if (isActive())
        {
            abort();
        }
        else {
            cameraUiInterface.getActivityInterface().getDialHandler().setDialEventListner(this);
            onEnterKeyUp();
        }
    }



    @Override
    public int getNavigationHelpID() {
        return 0;
    }

    @Override
    public void reset() {
        calcMaxBracketPicCount();
        updateBracketPicCount();
    }

    @Override
    public boolean prepare() {
        if (ExposureModeController.GetInstance().getExposureMode() == null)
            return false;
        if (isActive())
            abort();
        else
        {
            if (ExposureModeController.GetInstance().getExposureMode() != ExposureModeModel.ExposureModes.manual)
            {
                cameraUiInterface.showMessageDelayed("Scene mode must be set to manual");
                cameraUiInterface.getActivityInterface().getDialHandler().setDialEventListner((KeyEvents)cameraUiInterface);
                return false;
            }
            if (IsoController.GetInstance().getCurrentIso() == 0)
            {
                cameraUiInterface.showMessageDelayed("ISO must be set to manual");
                cameraUiInterface.getActivityInterface().getDialHandler().setDialEventListner((KeyEvents)cameraUiInterface);
                return false;
            }

            cameraUiInterface.setLeftViewVisibility(false);

            m_bracketPicCount = 3;
            m_bracketStep = 3;
            m_bracketShutterDelta = 0;
            updateBracketStep();

            // Remember current shutter speed
            m_bracketNeutralShutterIndex = CameraUtil.getShutterValueIndex(CameraInstance.GET().getShutterSpeed());
        }
        return true;
    }

    @Override
    public void startShooting() {
        Log.d(TAG,"startShooting");
        cameraUiInterface.hideHintMessage();
        cameraUiInterface.hideMessage();
        // Take first picture at set shutter speed
        cameraUiInterface.getActivityInterface().setBulbCapture(false);
        cameraUiInterface.getActivityInterface().setCaptureDoneEventListner(this);
        CameraInstance.GET().takePicture();
    }

    @Override
    public void abort() {
        cameraUiInterface.getActivityInterface().getMainHandler().removeCallbacks(m_countDownRunnable);
        //m_handler.removeCallbacks(m_timelapseRunnable);
        isActive = false;
        cameraUiInterface.showMessageDelayed("Bracketing finished");

        // Update controls
        cameraUiInterface.hideHintMessage();
        cameraUiInterface.setLeftViewVisibility(true);
        ExposureModeController.GetInstance().onValueChanged();
        DriveModeController.GetInstance().onValueChanged();

        cameraUiInterface.setActiveViewFlag(Preferences.GET().getViewFlags(cameraUiInterface.getActiveViewsFlag()));
        cameraUiInterface.updateViewVisibility();

        // Reset to previous shutter speed
        final int shutterDiff = m_bracketNeutralShutterIndex - CameraUtil.getShutterValueIndex(CameraInstance.GET().getShutterSpeed());
        if (shutterDiff != 0)
            CameraInstance.GET().adjustShutterSpeed(-shutterDiff);
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
        final int index = CameraUtil.getShutterValueIndex(CameraInstance.GET().getShutterSpeed());
        final int maxSteps = Math.min(index, CameraUtil.SHUTTER_SPEED_VALUES.length - 1 - index);
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
    public boolean onUpperDialChanged(int value) {

        return false;
    }

    @Override
    public boolean onLowerDialChanged(int value) {
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
            if (prepare()) {
                currentDialMode = BRACKET_STEP;
                updateBracketStep();
            }
        }
        else if(currentDialMode == BRACKET_STEP)
        {
            cameraUiInterface.showHintMessage(cameraUiInterface.getActivityInterface().getResString(R.string.icon_lowerDial)
                    + "to set picture count,"
                    + cameraUiInterface.getActivityInterface().getResString(R.string.icon_enterButton)
                    + " to confirm");
            currentDialMode = BRACKET_PICCOUNT;
            reset();
            updateBracketPicCount();
        }
        else
        {
            cameraUiInterface.getActivityInterface().getDialHandler().setDialEventListner((KeyEvents)cameraUiInterface);
            ShutterController.GetInstance().setShutterSpeedEventListner(this);
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

    @Override
    public void onCaptureDone() {
        Log.d(TAG,"onCaptureDone :" + m_bracketPicCount);
        if (--m_bracketPicCount == 0) {
            Log.d(TAG,"abort");
            abort();
            cameraUiInterface.getActivityInterface().setCaptureDoneEventListner(null);
            CameraInstance.GET().startPreview();
        }
        else
        {
            Log.d(TAG,"prepare next capture");
            retryCount = 0;
            m_bracketShutterDelta += m_bracketStep;
            final int shutterIndex = CameraUtil.getShutterValueIndex(CameraInstance.GET().getShutterSpeed());
            Log.d(TAG,"shutterIndex:" + shutterIndex);
            if (m_bracketShutterDelta % 2 == 0)
            {
                // Even, reduce shutter speed
                Log.d(TAG,"reduce");
                int newShutterspeed = shutterIndex - m_bracketShutterDelta;
                if (newShutterspeed < 0)
                    newShutterspeed = 0;
                Log.d(TAG,"newshutterindex:" +newShutterspeed + " bracketdelta:" + m_bracketShutterDelta);
                ShutterSpeedValue shutterSpeedValue = CameraUtil.SHUTTER_SPEED_VALUES[newShutterspeed];
                m_bracketNextShutterSpeed = shutterSpeedValue.getPair();
                CameraInstance.GET().adjustShutterSpeed(m_bracketShutterDelta);
            }
            else
            {
                Log.d(TAG,"increase");
                // Odd, increase shutter speed
                int newShutterspeed = shutterIndex + m_bracketShutterDelta;
                if (newShutterspeed > CameraUtil.SHUTTER_SPEED_VALUES.length)
                    newShutterspeed = CameraUtil.SHUTTER_SPEED_VALUES.length-1;
                Log.d(TAG,"newshutterindex:" +newShutterspeed + " bracketdelta:" + m_bracketShutterDelta);
                ShutterSpeedValue shutterSpeedValue = CameraUtil.SHUTTER_SPEED_VALUES[newShutterspeed];
                m_bracketNextShutterSpeed = shutterSpeedValue.getPair();
                CameraInstance.GET().adjustShutterSpeed(-m_bracketShutterDelta);
            }
        }

    }

    @Override
    public void onChanged() {
        Log.d(TAG, "onShutterSpeedChange");
        CameraEx.ShutterSpeedInfo shutterSpeedInfo = ShutterController.GetInstance().getShutterSpeedInfo();
        if (m_bracketPicCount > 0) {

            if (m_bracketNextShutterSpeed != null) {
                Log.d(TAG, "currentshutterspeed:" + shutterSpeedInfo.currentShutterSpeed_n + "/" + shutterSpeedInfo.currentShutterSpeed_d);
                Log.d(TAG, "bracketNextShutterSpeed:" + m_bracketNextShutterSpeed.first + "/" + m_bracketNextShutterSpeed.second);

                if (shutterSpeedInfo.currentShutterSpeed_n == m_bracketNextShutterSpeed.first &&
                        shutterSpeedInfo.currentShutterSpeed_d == m_bracketNextShutterSpeed.second) {
                    Log.d(TAG, "shutterspeed match start capture");
                    // Shutter speed adjusted, take next picture
                    startShooting();
                } else {
                    retryCount++;
                    Log.d(TAG, "shutterspeed does not match wait for next callback");
                    if (retryCount < MAX_RETRYS)
                        CameraInstance.GET().adjustShutterSpeed(m_bracketShutterDelta);
                    else //capture anyway
                        startShooting();
                }
            } else
                Log.d(TAG, "m_bracketNextShutterSpeed null");
        }
    }


}
