package com.obsidium.bettermanual.capture;

import android.util.Log;

import com.obsidium.bettermanual.KeyEvents;
import com.obsidium.bettermanual.Preferences;
import com.obsidium.bettermanual.R;
import com.obsidium.bettermanual.camera.CameraInstance;
import com.obsidium.bettermanual.camera.CaptureSession;
import com.obsidium.bettermanual.layout.CameraUIInterface;

/**
 * Created by KillerInk on 11.10.2017.
 */

public class CaptureModeBulb extends CaptureMode implements CaptureSession.CaptureDoneEvent, KeyEvents {

    private static CaptureModeBulb captureModeBulb;

    public static CaptureModeBulb GetInstance()
    {
        return captureModeBulb;
    }

    public static void CREATE(CameraUIInterface cameraUiInterface)
    {
        captureModeBulb = new CaptureModeBulb(cameraUiInterface);
    }

    public static void CLEAR()
    {
        captureModeBulb = null;
    }

    private final String TAG = CaptureModeBulb.class.getSimpleName();
    private long bulbCaptureTime = 0;

    private final int DIAL_STATE_NOTHING = 0;
    private final int DIAL_STATE_BULBTIME = 1;
    private int currentdial = DIAL_STATE_NOTHING;



    private CaptureModeBulb(CameraUIInterface cameraUiInterface) {
        super(cameraUiInterface);
    }



    @Override
    public void reset() {
        Log.d(TAG,"reset");
        bulbCaptureTime = 0;
        currentdial = DIAL_STATE_BULBTIME;
        updateBulbTime();
    }

    @Override
    public boolean prepare() {
        Log.d(TAG,"prepare");
        if (isActive())
            abort();
        currentdial = DIAL_STATE_BULBTIME;
        bulbCaptureTime = Preferences.GET().getBulbTime();
        updateBulbTime();
        cameraUIInterface.showHintMessage(cameraUIInterface.getActivityInterface().getResString(R.string.icon_lowerDial) +
                " to set Bulb Duration, "
                + cameraUIInterface.getActivityInterface().getResString(R.string.icon_enterButton)
                + " to start");
        return true;
    }

    @Override
    public void startShooting() {
        Log.d(TAG,"startShooting");
        Preferences.GET().setBulbTime(bulbCaptureTime);
        cameraUIInterface.hideHintMessage();
        cameraUIInterface.hideMessage();

        cameraUIInterface.getActivityInterface().setBulbCapture(true);
        cameraUIInterface.getActivityInterface().setCaptureDoneEventListner(this);
        CameraInstance.GET().takePicture();
        if (bulbCaptureTime > 0);
            cameraUIInterface.getActivityInterface().getMainHandler().postDelayed(cancelPictureRunner,bulbCaptureTime);
    }

    private Runnable cancelPictureRunner = () -> {
        cameraUIInterface.getActivityInterface().cancelBulbCapture();
        onCaptureDone();
    };

    @Override
    public void abort() {
        Log.d(TAG,"abort");
        isActive = false;
        Log.d(TAG,"remove m_countDownRunnable");
        cameraUIInterface.getActivityInterface().getMainHandler().removeCallbacks(m_countDownRunnable);
        cameraUIInterface.showMessageDelayed("Bulb finished");
        CameraInstance.GET().enableHwShutterButton();
        CameraInstance.GET().startPreview();
    }

    @Override
    public void incrementInterval() {
        if (bulbCaptureTime < 1000) //ms
            bulbCaptureTime += 100;
        else if(bulbCaptureTime > 1000 * 60)//min
            bulbCaptureTime += 1000*60;
        else
            bulbCaptureTime += 1000;//sec
        updateBulbTime();
    }

    @Override
    public void decrementInterval() {
        if (bulbCaptureTime > 0)
        {
            if (bulbCaptureTime <= 1000) //ms
                bulbCaptureTime -= 100;
            else if(bulbCaptureTime > 1000 * 60) //min
                bulbCaptureTime -= 1000*60;
            else
                bulbCaptureTime -= 1000; // sec
        }
        updateBulbTime();
    }

    @Override
    public void incrementPicCount() {

    }

    @Override
    public void decrementPicCount() {

    }


    private void updateBulbTime()
    {
        if (bulbCaptureTime == 0)
            cameraUIInterface.showMessage("Unlimited");
        else if (bulbCaptureTime < 1000)
            cameraUIInterface.showMessage(String.format("%d msec", bulbCaptureTime));
        else if (bulbCaptureTime == 1000)
            cameraUIInterface.showMessage("1 second");
        else if (bulbCaptureTime /1000 /60 < 1)
            cameraUIInterface.showMessage(String.format("%d seconds", bulbCaptureTime / 1000));
        else
            cameraUIInterface.showMessage(String.format("%d min", bulbCaptureTime /1000 / 60) );
    }

    @Override
    public void onCaptureDone() {
        Log.d(TAG,"onCaptureDone");
        cameraUIInterface.getActivityInterface().setCaptureDoneEventListner(null);
        abort();
    }

    @Override
    public boolean onUpperDialChanged(int value) {
        return false;
    }

    @Override
    public boolean onLowerDialChanged(int value) {
        if (currentdial == DIAL_STATE_BULBTIME)
        {
            if (value <0)
                decrementInterval();
            else
                incrementInterval();
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
        if (isActive() && currentdial == DIAL_STATE_NOTHING) {
            abort();
            return false;
        }
        if (currentdial == DIAL_STATE_NOTHING) {
            if (prepare())
                updateBulbTime();
        }
        else if (currentdial == DIAL_STATE_BULBTIME)
        {
            Log.d(TAG, "onEnterKeyDown setDefaultDialListner");
            cameraUIInterface.getActivityInterface().getDialHandler().setDialEventListener((KeyEvents) cameraUIInterface);
            Log.d(TAG, "onEnterKeyDown startCountDown");
            startCountDown();
            currentdial = DIAL_STATE_NOTHING;
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
    public void setColorToView(Integer color) {

    }

    @Override
    public int getNavigationHelpID() {
        return 0;
    }
}
