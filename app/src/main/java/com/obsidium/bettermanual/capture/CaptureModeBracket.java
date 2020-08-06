package com.obsidium.bettermanual.capture;

import android.util.Log;

import com.obsidium.bettermanual.CameraUtil;
import com.obsidium.bettermanual.KeyEvents;
import com.obsidium.bettermanual.Preferences;
import com.obsidium.bettermanual.R;
import com.obsidium.bettermanual.camera.CameraInstance;
import com.obsidium.bettermanual.camera.CaptureSession;
import com.obsidium.bettermanual.controller.DriveModeController;
import com.obsidium.bettermanual.controller.ExposureModeController;
import com.obsidium.bettermanual.controller.IsoController;
import com.obsidium.bettermanual.controller.ShutterController;
import com.obsidium.bettermanual.layout.CameraUIInterface;
import com.obsidium.bettermanual.model.ExposureModeModel;

public class CaptureModeBracket extends CaptureMode implements  ShutterController.ShutterSpeedEvent, KeyEvents, CaptureSession.CaptureDoneEvent {

    private final String TAG = CaptureModeBracket.class.getSimpleName();
    // Bracketing
    private int             m_bracketStep;  // in 1/3 stops
    private int             m_bracketMaxPicCount;
    private int             m_bracketPicCount;
    private int             m_bracketNeutralShutterIndex;
    private final int BRACKET_NON = 0;
    private final int BRACKET_STEP = 1;
    private final int BRACKET_PICCOUNT = 2;
    private int currentDialMode = BRACKET_NON;


    private BracketShutterController bracketShutterController;


    public CaptureModeBracket(CameraUIInterface cameraUiInterface)
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
            cameraUIInterface.getActivityInterface().getDialHandler().setDialEventListener(this);
            onEnterKeyUp();
        }
    }



    @Override
    public int getNavigationHelpID() {
        return R.string.view_startBracket;
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
                cameraUIInterface.showMessageDelayed("Scene mode must be set to manual");
                cameraUIInterface.getActivityInterface().getDialHandler().setDialEventListener((KeyEvents) cameraUIInterface);
                return false;
            }
            if (IsoController.GetInstance().getCurrentIso() == 0)
            {
                cameraUIInterface.showMessageDelayed("ISO must be set to manual");
                cameraUIInterface.getActivityInterface().getDialHandler().setDialEventListener((KeyEvents) cameraUIInterface);
                return false;
            }

            cameraUIInterface.setLeftViewVisibility(false);

            m_bracketPicCount = 3;
            m_bracketStep = 3;
            updateBracketStep();

            // Remember current shutter speed
            m_bracketNeutralShutterIndex = CameraUtil.getShutterValueIndex(CameraInstance.GET().getShutterSpeed());
        }
        return true;
    }

    @Override
    public void startShooting() {
        Log.d(TAG,"startShooting");
        cameraUIInterface.hideHintMessage();
        cameraUIInterface.hideMessage();
        // Take first picture at set shutter speed
        cameraUIInterface.getActivityInterface().setBulbCapture(false);
        bracketShutterController = new BracketShutterController(m_bracketPicCount,m_bracketStep);
        bracketShutterController.captureRange();
        //CameraInstance.GET().takePicture();
    }

    @Override
    public void abort() {
        if (bracketShutterController != null)
            bracketShutterController.abort();
        m_bracketPicCount = 0;
        cameraUIInterface.getActivityInterface().getMainHandler().removeCallbacks(m_countDownRunnable);
        cameraUIInterface.getActivityInterface().setCaptureDoneEventListner(null);
        ShutterController.GetInstance().setShutterSpeedEventListner(null);
        currentDialMode = BRACKET_NON;
        //m_handler.removeCallbacks(m_timelapseRunnable);
        isActive = false;
        cameraUIInterface.showMessageDelayed("Bracketing finished");
        CameraInstance.GET().enableHwShutterButton();
        CameraInstance.GET().startPreview();

        // Update controls
        cameraUIInterface.hideHintMessage();
        cameraUIInterface.setLeftViewVisibility(true);
        ExposureModeController.GetInstance().onValueChanged();
        DriveModeController.GetInstance().onValueChanged();

        cameraUIInterface.setActiveViewFlag(Preferences.GET().getViewFlags(cameraUIInterface.getActiveViewsFlag()));
        cameraUIInterface.updateViewVisibility();

        // Reset to previous shutter speed
        final int shutterDiff = m_bracketNeutralShutterIndex - CameraUtil.getShutterValueIndex(CameraInstance.GET().getShutterSpeed());
        if (shutterDiff != 0)
            CameraInstance.GET().adjustShutterSpeed(-shutterDiff);
    }

    @Override
    public void incrementInterval() {
        if (m_bracketStep < 9)
        {
            ++m_bracketStep;
            updateBracketStep();
        }
    }

    @Override
    public void decrementInterval() {
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
        Log.d(TAG, "calcMaxBracketPicCount Index:" + index +" maxsteps:" + maxSteps +" bracketMaxPicCount:" + m_bracketMaxPicCount);
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
        cameraUIInterface.showMessage(String.format("%d.%dEV", m_bracketStep / 3, ev));
    }

    protected void updateBracketPicCount()
    {
        cameraUIInterface.showMessage(String.format("%d pictures", m_bracketPicCount));
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
                decrementInterval();
            else
                incrementInterval();
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
            cameraUIInterface.showHintMessage(cameraUIInterface.getActivityInterface().getResString(R.string.icon_lowerDial)
                    + "to set picture count,"
                    + cameraUIInterface.getActivityInterface().getResString(R.string.icon_enterButton)
                    + " to confirm");
            currentDialMode = BRACKET_PICCOUNT;
            reset();
        }
        else
        {
            cameraUIInterface.getActivityInterface().getDialHandler().setDialEventListener((KeyEvents) cameraUIInterface);
            ShutterController.GetInstance().setShutterSpeedEventListner(this);
            cameraUIInterface.getActivityInterface().setCaptureDoneEventListner(this);
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
        bracketShutterController.NotifyWaitlock();
    }

    @Override
    public void onChanged() {
        Log.d(TAG, "onShutterSpeedChange");

        bracketShutterController.NotifyWaitlock();

    }


    private class BracketShutterController
    {
        int[] shutterSpeedIndexesToCapture;
        private Thread captureThread;
        private Object waitlock = new Object();

        public void NotifyWaitlock()
        {
            synchronized (waitlock)
            {
                waitlock.notify();
            }
        }

        public BracketShutterController(int pictureCount, int bracketSteps)
        {
            int arrapos = 0;
            shutterSpeedIndexesToCapture = new int[pictureCount];
            final int shutterIndex = CameraUtil.getShutterValueIndex(CameraInstance.GET().getShutterSpeed());
            int stepsize = (pictureCount-1) /2;
            for(int i = stepsize; i >= 1; i--)
            {
                shutterSpeedIndexesToCapture[arrapos++] = shutterIndex - bracketSteps*i;
            }
            shutterSpeedIndexesToCapture[arrapos++] = shutterIndex;
            for(int i = 1; i <= stepsize; i++)
            {
                shutterSpeedIndexesToCapture[arrapos++] = shutterIndex + bracketSteps*i;
            }
        }

        public void abort()
        {
            captureThread.interrupt();
        }

        public void captureRange()
        {
            captureThread = new Thread(){
                @Override
                public void run() {
                    int currentPicture =0;
                    while (currentPicture < shutterSpeedIndexesToCapture.length && !Thread.currentThread().isInterrupted())
                    {
                        int newIndex = shutterSpeedIndexesToCapture[currentPicture];
                        int shutterIndex = CameraUtil.getShutterValueIndex(CameraInstance.GET().getShutterSpeed());
                        while (newIndex != shutterIndex && !Thread.currentThread().isInterrupted())
                        {
                            synchronized (waitlock)
                            {
                                final int toset = newIndex - shutterIndex;
                                cameraUIInterface.getActivityInterface().getMainHandler().post(()-> {
                                    CameraInstance.GET().adjustShutterSpeed(-toset);
                                });

                                //wait for next shutterspeed changed
                                try {
                                    waitlock.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            shutterIndex = CameraUtil.getShutterValueIndex(CameraInstance.GET().getShutterSpeed());
                        }
                        cameraUIInterface.getActivityInterface().getMainHandler().post(()-> {
                            CameraInstance.GET().takePicture();
                        });

                        //wait for capture complete
                        synchronized (waitlock)
                        {
                            try {
                                waitlock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        currentPicture++;
                    }
                    cameraUIInterface.getActivityInterface().getMainHandler().post(()-> {
                        Log.d(TAG,"abort");
                        CaptureModeBracket.this.abort();
                    });
                }
            };
            captureThread.start();
        }
    }
}
