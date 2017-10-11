package com.obsidium.bettermanual.camera;

import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

import com.sony.scalar.hardware.CameraEx;
import com.sony.scalar.hardware.CameraSequence;

import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;


public class CameraInstance extends CameraInternalEventListner implements  CameraSequence.ShutterSequenceCallback   {
    private final String TAG = CameraInstance.class.getSimpleName();

//    public CameraSequence cameraSequence;

    private SurfaceHolder surfaceHolder;

    public CameraInstance() {
        super();
    }


    public void startCamera(CameraEx.ShutterListener shutterListener) {
        CameraEx.OpenOptions options = new CameraEx.OpenOptions();
        options.setPreview(true);
        Log.d(TAG, "Open Cam");
        m_camera = CameraEx.open(0, null);
        /*cameraSequence = CameraSequence.open(m_camera);
        setOptions(null);
        cameraSequence.setShutterSequenceCallback(this);*/
        Log.d(TAG, "Cam open");
        cameraIsOpen = true;
        autoPictureReviewControl = new CameraEx.AutoPictureReviewControl();
        m_camera.setAutoPictureReviewControl(getAutoPictureReviewControls());
        autoPictureReviewControl.setPictureReviewTime(0);
        autoPictureReviewControl.cancelAutoPictureReview();

        initParameters();
        initListners();

        m_camera.withSupportedParameters(false);
        fireOnCameraOpen(true);

    }

    public void initParameters()
    {
        //dumpParameter();
    }

    /*public void setOptions(CameraSequence.Options paramOptions)
    {

            if (paramOptions == null) {
                paramOptions = new CameraSequence.Options();
            }
            paramOptions.setOption("AUTO_RELEASE_LOCK_ENABLED", true);
        cameraSequence.setReleaseLock(false);
    }*/

    private void dumpParameter() {
        StringTokenizer localStringTokenizer = new StringTokenizer(((Camera.Parameters)getParameters()).flatten(), ";");
        while (localStringTokenizer.hasMoreElements())
            Log.d(TAG, localStringTokenizer.nextToken());

        List<String> tmp = null;
        if (isImageStabSupported())
        {
            Log.d(TAG,"getSupportedImageStabModes");
            logList(getSupportedImageStabModes());
        }
        if (isLiveSlowShutterSupported()) {
            Log.d(TAG, "getSupportedLiveSlowShutterModes");
            logArray(getSupportedLiveSlowShutterModes());
        }
    }

    private void logList(List<String> list)
    {
        String st = new String();
        for (String s : list)
            st += s+",";
        Log.d(TAG,st);
    }
    private void logArray(String[] list)
    {
        String st = new String();
        for (String s : list)
            st += s+",";
        Log.d(TAG,st);
    }

    public void closeCamera() {
        cameraIsOpen = false;
        Log.d(TAG, "closeCamera");
        removeListners();
        /*cameraSequence.setShutterSequenceCallback(null);
        cameraSequence.release();*/
        m_camera.getNormalCamera().stopPreview();
        m_camera.getNormalCamera().release();
        m_camera.release();
        m_camera = null;
    }

    public void setSurfaceHolder(SurfaceHolder surface) {
        this.surfaceHolder = surface;
    }

    public void startPreview() {
        m_camera.getNormalCamera().startPreview();
    }

    public void stopPreview() {
        m_camera.getNormalCamera().stopPreview();
    }

    public void enableHwShutterButton() {
        m_camera.startDirectShutter();

    }

    public void disableHwShutterButton() {
        m_camera.stopDirectShutter(null);
    }

    public void cancleCapture()
    {
        m_camera.cancelTakePicture();
    }

    public void takePicture()
    {
        //hw shutter button must get stopped else burstableTakePicture does not trigger
        m_camera.stopDirectShutter(new CameraEx.DirectShutterStoppedCallback() {
            @Override
            public void onShutterStopped(CameraEx cameraEx) {
                m_camera.burstableTakePicture();
            }
        });
    }

    @Override
    public void onShutterSequence(CameraSequence.RawData rawData, CameraSequence cameraSequence) {
        Log.d(TAG,"onShutterSequence");
        m_camera.cancelTakePicture();
        cameraSequence.setReleaseLock(true);
    }

  /*  @Override
    public void onSplitShutterSequence(CameraSequence.RawData rawData, CameraSequence.SplitExposureProgressInfo splitExposureProgressInfo, CameraSequence cameraSequence) {
        Log.d(TAG, "onSplitShutterSequence();");
        cameraSequence.setReleaseLock(false);
    }

    @Override
    public void onShutterSequence(CameraSequence.RawData rawData, CameraSequence cameraSequence) {
        Log.d(TAG,"onShutterSequence");
        cameraSequence.setReleaseLock(false);
    }*/
}

