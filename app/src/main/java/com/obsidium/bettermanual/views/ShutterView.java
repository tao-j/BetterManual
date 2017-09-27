package com.obsidium.bettermanual.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import com.obsidium.bettermanual.MainActivity;
import com.github.killerink.camera.CaptureSession;
import com.obsidium.bettermanual.CameraUtil;
import com.obsidium.bettermanual.R;

public class ShutterView extends BaseTextView implements CaptureSession.CaptureDoneEvent {

    private final String TAG = ShutterView.class.getSimpleName();

    @Override
    public void setIn_DecrementValue(int value) {
        onScrolled(value);
    }

    @Override
    public String getNavigationString() {
        if (cameraUiInterface.getExposureMode().get() == ExposureModeView.ExposureModes.manual || cameraUiInterface.getExposureMode().get() == ExposureModeView.ExposureModes.shutter)
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
                startBulbCapture();
                return false;

            } else if (cameraUiInterface.getActivityInterface().isBulbCapture()) {

                stopBulbCapture();
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
