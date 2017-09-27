package com.obsidium.bettermanual.camera;

import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

import com.sony.scalar.hardware.CameraEx;

import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;


public class CameraInstance extends BaseCamera  {
    private final String TAG = CameraInstance.class.getSimpleName();

    private SurfaceHolder surfaceHolder;

    public CameraInstance() {
        super();
    }


    public void startCamera(CameraEx.ShutterListener shutterListener) {
        CameraEx.OpenOptions options = new CameraEx.OpenOptions();
        options.setPreview(true);
        Log.d(TAG, "Open Cam");
        m_camera = CameraEx.open(0, null);
        Log.d(TAG, "Cam open");
        cameraIsOpen = true;

        initParameters();
        autoPictureReviewControl = new CameraEx.AutoPictureReviewControl();
        m_camera.setAutoPictureReviewControl(getAutoPictureReviewControls());
        fireOnCameraOpen(true);

    }

    public void initParameters()
    {
        parameters = m_camera.getNormalCamera().getParameters();
        modifier = m_camera.createParametersModifier(parameters);
        dumpParameter();
    }


    private void dumpParameter() {
        StringTokenizer localStringTokenizer = new StringTokenizer(((Camera.Parameters)parameters).flatten(), ";");
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
        m_camera.cancelTakePicture();
        m_camera.setAutoFocusStartListener(null);
        m_camera.setAutoFocusDoneListener(null);
        m_camera.setPreviewStartListener(null);
        m_camera.setAutoPictureReviewControl(null);
        m_camera.setPreviewAnalizeListener(null);
        m_camera.setAutoISOSensitivityListener(null);
        m_camera.setShutterListener(null);
        m_camera.setApertureChangeListener(null);
        m_camera.setProgramLineRangeOverListener(null);
        m_camera.setPreviewMagnificationListener(null);
        m_camera.setFocusDriveListener(null);
        m_camera.setShutterSpeedChangeListener(null);

        m_camera.getNormalCamera().stopPreview();
        m_camera.release();
        m_camera = null;
    }

    public void setSurfaceHolder(SurfaceHolder surface) {
        this.surfaceHolder = surface;
    }

    public void startDisplay() {
        m_camera.getNormalCamera().startPreview();
    }

    public void stopDisplay() {
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
}

