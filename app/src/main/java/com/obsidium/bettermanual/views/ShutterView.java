package com.obsidium.bettermanual.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import com.obsidium.bettermanual.CameraUiInterface;
import com.obsidium.bettermanual.CameraUtil;
import com.obsidium.bettermanual.MainActivity;
import com.obsidium.bettermanual.R;
import com.obsidium.bettermanual.camera.CaptureSession;
import com.obsidium.bettermanual.capture.CaptureModeBulb;

public class ShutterView extends BaseTextView implements CaptureSession.CaptureDoneEvent {

    private final String TAG = ShutterView.class.getSimpleName();
    private CaptureModeBulb captureModeBulb;

    @Override
    public void setIn_DecrementValue(int value) {
        onScrolled(value);
    }

    @Override
    public String getNavigationString() {
        ExposureModeView view = cameraUiInterface.getExposureMode();
        if (view == null)
            return getResources().getString(R.string.view_shutter_navigation_disabled);
        if (view.get() == ExposureModeView.ExposureModes.manual || view.get() == ExposureModeView.ExposureModes.shutter)
            return getResources().getString(R.string.view_shutter_navigation_enable);
        return getResources().getString(R.string.view_shutter_navigation_disabled);
    }


    // Shutter speed
    private boolean         m_notifyOnNextShutterSpeedChange;
    public ShutterView(Context context) {
        super(context);
    }

    public ShutterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShutterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setCameraUiInterface(CameraUiInterface cameraUiInterface) {
        super.setCameraUiInterface(cameraUiInterface);
        captureModeBulb = new CaptureModeBulb(cameraUiInterface);
    }

    @Override
    public void onScrolled(int distance) {
        if (distance > 0)
            cameraUiInterface.getActivityInterface().getCamera().decrementShutterSpeed();
        else
            cameraUiInterface.getActivityInterface().getCamera().incrementShutterSpeed();
        //cameraUiInterface.getActivityInterface().getBackHandler().post(new ShutterSetRunner(distance));
    }

    @Override
    public boolean onClick() {
        if (cameraUiInterface.getExposureMode().get() == ExposureModeView.ExposureModes.aperture && !getText().equals("BULB"))
        {
            // Set minimum shutter speed
            cameraUiInterface.getActivityInterface().loadFragment(MainActivity.FRAGMENT_MIN_SHUTTER);
            return true;
        }
        else if (getText().equals("BULB")) {
            if (!cameraUiInterface.getActivityInterface().isBulbCapture()) {
                cameraUiInterface.getActivityInterface().getDialHandler().setDialEventListner(captureModeBulb);
                captureModeBulb.prepare();
                return false;

            } else if (cameraUiInterface.getActivityInterface().isBulbCapture()) {
                stopBulbCapture();
                captureModeBulb.abort();
                return false;
            }
        }
        return false;
    }

    private void stopBulbCapture() {
        cameraUiInterface.getActivityInterface().cancelBulbCapture();
        Log.d(TAG, "Stop BULB");
    }

    private void startBulbCapture()
    {
        captureModeBulb.prepare();
        cameraUiInterface.getActivityInterface().setBulbCapture(true);
        cameraUiInterface.getActivityInterface().setCaptureDoneEventListner(this);
        cameraUiInterface.getActivityInterface().getCamera().takePicture();
        Log.d(TAG, "Start BULB");
    }


    public void updateShutterSpeed(int n, int d)
    {
        final String text = CameraUtil.formatShutterSpeed(n, d);
        setText(text);
        if (m_notifyOnNextShutterSpeedChange)
        {
            cameraUiInterface.showMessageDelayed(text);
            m_notifyOnNextShutterSpeedChange = false;
        }
    }

    @Override
    public void onCaptureDone() {

    }
}
