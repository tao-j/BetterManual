package com.github.killerink.camera;

import com.sony.scalar.hardware.CameraEx;

/**
 * Created by KillerInk on 10.09.2017.
 */

public class CaptureSession implements Runnable, CameraEx.ShutterListener {

    private CameraInstance cameraInstance;
    private boolean isBulbCapture = false;
    private boolean isCaptureInProgress = false;
    private CaptureDoneEvent eventListner;

    public interface CaptureDoneEvent
    {
        void onCaptureDone();
    }

    public CaptureSession(CameraInstance cameraInstance, boolean isBulbCapture, CaptureDoneEvent eventListner)
    {
        this.cameraInstance = cameraInstance;
        this.isBulbCapture = isBulbCapture;
        this.eventListner = eventListner;
        cameraInstance.setShutterListener(this);
    }

    public boolean isCaptureInProgress()
    {
        return isCaptureInProgress;
    }

    public boolean isBulbCapture()
    {
        return isBulbCapture;
    }

    public void cancelBulbCapture()
    {
        isBulbCapture = false;
    }

    @Override
    public void run() {
        if (isCaptureInProgress)
            return;
        cameraInstance.takePicture();
        isCaptureInProgress = true;
    }

    @Override
    public void onShutter(int i, CameraEx cameraEx) {
        if (!isBulbCapture) {
            cameraInstance.cancelTakePicture();
            isCaptureInProgress = false;
            if (eventListner != null)
                eventListner.onCaptureDone();
        }
    }
}
