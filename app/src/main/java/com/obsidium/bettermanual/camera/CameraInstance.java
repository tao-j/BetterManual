package com.obsidium.bettermanual.camera;

import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;

import com.sony.scalar.hardware.CameraEx;
import com.sony.scalar.hardware.CameraSequence;

import java.util.List;
import java.util.StringTokenizer;


public class CameraInstance extends CameraInternalEventListner implements  CameraSequence.ShutterSequenceCallback   {
    private final String TAG = CameraInstance.class.getSimpleName();

//    public CameraSequence cameraSequence;

    final int SETSURFACE = 0;
    private static CameraInstance INSTANCE = new CameraInstance();
    private SurfaceHolder surfaceHolder;


    private CameraInstance() {
        super();
    }

    public static CameraInstance GET()
    {
        return INSTANCE;
    }

    public void initHandler(Looper looper)
    {
        cameraHandler = new CameraHandler(looper);
    }




    public void startCamera() {
/*        CameraEx.OpenOptions options = new CameraEx.OpenOptions();
        options.setPreview(true);*/
        Log.d(TAG, "Open Cam");
        m_camera = CameraEx.open(0, null);
        cameraIsOpen = true;
        /*cameraSequence = CameraSequence.open(m_camera);
        setOptions(null);
        cameraSequence.setShutterSequenceCallback(this);*/
        Log.d(TAG, "Cam open");
        cameraHandler.sendMessage(cameraHandler.obtainMessage(MSG_INIT_CAMERA));
        fireOnCameraOpen(true);

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
        surfaceHolder = null;
    }

    public void setSurfaceHolder(SurfaceHolder surface) {
        this.surfaceHolder = surface;
    }

    public void startPreview() {
        cameraHandler.sendMessage(cameraHandler.obtainMessage(START_PREVIEW));
    }

    public void stopPreview() {
        cameraHandler.sendMessage(cameraHandler.obtainMessage(STOP_PREVIEW));
    }

    public void enableHwShutterButton() {
        m_camera.startDirectShutter();

    }

    public void disableHwShutterButton() {
        m_camera.stopDirectShutter(null);
    }

    public void cancelCapture()
    {
        cameraHandler.sendMessage(cameraHandler.obtainMessage(CANCEL_CAPTURE));
    }

    public void takePicture()
    {
        cameraHandler.sendMessage(cameraHandler.obtainMessage(CAPTURE_IMAGE));

    }

    @Override
    public void onShutterSequence(CameraSequence.RawData rawData, CameraSequence cameraSequence) {
        Log.d(TAG,"onShutterSequence");
        m_camera.cancelTakePicture();
        //cameraSequence.setReleaseLock(true);
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


    class CameraHandler extends Handler
    {

        public CameraHandler(Looper looper)
        {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case MSG_INIT_CAMERA:
                    initCamera();
                    break;
                case CAPTURE_IMAGE:
                    //hw shutter button must get stopped else burstableTakePicture does not trigger
                    m_camera.stopDirectShutter(new CameraEx.DirectShutterStoppedCallback() {
                        @Override
                        public void onShutterStopped(CameraEx cameraEx) {
                            m_camera.burstableTakePicture();
                        }
                    });
                    break;
                case CANCEL_CAPTURE:
                    m_camera.cancelTakePicture();
                    break;
                case START_PREVIEW:
                    m_camera.getNormalCamera().startPreview();
                    break;
                case STOP_PREVIEW:
                    m_camera.getNormalCamera().stopPreview();
                    break;
                case SET_EV:
                    Camera.Parameters parameters = getEmptyParameters();
                    parameters.setExposureCompensation(msg.arg1);
                    setParameters(parameters);
                    break;
                case MSG_SET_LONGEXPONR:
                    parameters = getEmptyParameters();
                    CameraEx.ParametersModifier modifier = m_camera.createParametersModifier(parameters);
                    modifier.setLongExposureNR((Boolean) msg.obj);
                    setParameters(parameters);
                    break;
                case MSG_SET_FOCUSMODE:
                    parameters = getEmptyParameters();
                    parameters.setFocusMode((String)msg.obj);
                    setParameters(parameters);
                    break;
                case MSG_SET_SCENEMODE:
                    parameters = getEmptyParameters();
                    parameters.setSceneMode((String)msg.obj);
                    setParameters(parameters);
                    break;
                case MSG_SET_DRIVEMODE:
                    parameters = getEmptyParameters();
                    modifier = m_camera.createParametersModifier(parameters);
                    modifier.setDriveMode((String)msg.obj);
                    setParameters(parameters);
                    break;
                case MSG_SET_IMAGEASPECTRATIO:
                    parameters = getEmptyParameters();
                    modifier = m_camera.createParametersModifier(parameters);
                    modifier.setImageAspectRatio((String)msg.obj);
                    setParameters(parameters);
                    break;
                case MSG_SET_IMAGEQUALITY:
                    parameters = getEmptyParameters();
                    modifier = m_camera.createParametersModifier(parameters);
                    modifier.setPictureStorageFormat((String)msg.obj);
                    setParameters(parameters);
                    break;
                case MSG_SET_BURSTDRIVESPEED:
                    parameters = getEmptyParameters();
                    modifier = m_camera.createParametersModifier(parameters);
                    modifier.setBurstDriveSpeed((String)msg.obj);
                    setParameters(parameters);
                    break;
                case SET_AUTO_SHUTTER_SPEED_LOW_LIMIT:
                    parameters = getEmptyParameters();
                    modifier = m_camera.createParametersModifier(parameters);
                    modifier.setAutoShutterSpeedLowLimit(msg.arg1);
                    setParameters(parameters);
                    break;
                case SET_SELF_TIMER:
                    parameters = getEmptyParameters();
                    modifier = m_camera.createParametersModifier(parameters);
                    modifier.setSelfTimer(msg.arg1);
                    setParameters(parameters);
                    break;
                case SET_ISO:
                    parameters = getEmptyParameters();
                    modifier = m_camera.createParametersModifier(parameters);
                    modifier.setISOSensitivity(msg.arg1);
                    setParameters(parameters);
                    break;
                case INCREASE_SHUTTER:
                        m_camera.incrementShutterSpeed();
                    break;
                case DECREASE_SHUTTER:
                    m_camera.decrementShutterSpeed();
                    break;
                case INCREASE_APERTURE:
                    m_camera.incrementAperture();
                    break;
                case DECREASE_APERTURE:
                    m_camera.decrementAperture();
                    break;
                case MSG_SET_IMAGESTABILISATION:
                    parameters = getEmptyParameters();
                    modifier = m_camera.createParametersModifier(parameters);
                    modifier.setAntiHandBlurMode((String) msg.obj);
                    setParameters(parameters);
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void initCamera()
    {
        autoPictureReviewControl = new CameraEx.AutoPictureReviewControl();
        m_camera.setAutoPictureReviewControl(getAutoPictureReviewControls());
        autoPictureReviewControl.setPictureReviewTime(0);
        autoPictureReviewControl.cancelAutoPictureReview();

        initParameters();
        initListners();

        //when false cameraparameters contains only the active parameters, but supported stuff is missing
        m_camera.withSupportedParameters(true);
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
}

