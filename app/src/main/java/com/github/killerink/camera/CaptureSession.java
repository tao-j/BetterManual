package com.github.killerink.camera;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.sony.scalar.graphics.AvindexGraphics;
import com.sony.scalar.hardware.CameraEx;
import com.sony.scalar.hardware.CameraSequence;
import com.sony.scalar.media.AvindexFactory;
import com.sony.scalar.provider.AvindexStore;
import com.sony.scalar.provider.AvindexUpdateObserver;

/**
 * Created by KillerInk on 10.09.2017.
 */

public class CaptureSession implements Runnable, CameraEx.ShutterListener {

    private CameraInstance cameraInstance;
    private boolean isBulbCapture = false;
    private boolean isCaptureInProgress = false;
    private CaptureDoneEvent eventListner;
    private final String TAG = CaptureSession.class.getSimpleName();
    private CameraEx cameraEx;

    public interface CaptureDoneEvent
    {
        void onCaptureDone();
    }

    public CaptureSession(CameraInstance cameraInstance,CameraEx cameraEx )
    {
        this.cameraInstance = cameraInstance;
        this.cameraEx = cameraEx;
        //cameraInstance.setShutterListener(this);
    }

    public void setBulbCapture(boolean bulbCapture)
    {
        this.isBulbCapture = bulbCapture;
    }

    public void setEventListner(CaptureDoneEvent eventListner)
    {
        this.eventListner = eventListner;
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
        cameraEx.cancelTakePicture();
    }

    @Override
    public void run() {
        if (isCaptureInProgress)
            return;
        //cameraEx.stopDirectShutter(null);
        cameraEx.burstableTakePicture();
        isCaptureInProgress = true;
    }

    /**
     * Returned from camera when a capture is done
     * STATUS_CANCELED = 1;
     * STATUS_ERROR = 2;
     * STATUS_OK = 0;
     * @param i code
     * @param cameraEx2 did capture Image
     */
    @Override
    public void onShutter(int i, CameraEx cameraEx2) {
        Log.d(TAG, "onShutter:" + logCaptureCode(i)+ " isBulb:" + isBulbCapture);
        if (!isBulbCapture) {
            this.cameraEx.cancelTakePicture();
            this.cameraEx.getNormalCamera().cancelAutoFocus();
            //this.cameraEx.startDirectShutter();
            isCaptureInProgress = false;
            if (eventListner != null)
                eventListner.onCaptureDone();
        }
    }

    private String logCaptureCode(int status)
    {
        switch (status)
        {
            case 1:
                return "Canceled";
            case 2:
                return "Error";
            default:
                return "OK";
        }
    }
}