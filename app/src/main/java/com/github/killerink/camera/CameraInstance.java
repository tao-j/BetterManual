package com.github.killerink.camera;

import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
import android.view.SurfaceHolder;

import com.sony.scalar.hardware.CameraEx;

import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;


public class CameraInstance extends CameraInternalEventImpl implements SurfaceHolder.Callback {
    private final String TAG = CameraInstance.class.getSimpleName();

    private boolean isSurfaceCreated = false;
    private SurfaceHolder surfaceHolder;

    public CameraInstance(HandlerThread hthread) {
        super();
        cameraHandler = new BackGroundHandler(hthread.getLooper());
        uiHandler = new MainHandler(Looper.getMainLooper());

    }

    public void startCamera() {
        cameraHandler.post(cameraOpenRunner);
    }

    private Runnable cameraOpenRunner = new Runnable() {
        @Override
        public void run() {
            CameraEx.OpenOptions options = new CameraEx.OpenOptions();
            Log.d(TAG, "Open Cam");
            options.setPreview(true);
            m_camera = CameraEx.open(0, options);
            Log.d(TAG, "Cam open");
            parameters = m_camera.getNormalCamera().getParameters();
            modifier = m_camera.createParametersModifier(parameters);
            dumpParameter();

            m_camera.setAutoFocusStartListener(CameraInstance.this);
            m_camera.setAutoFocusDoneListener(CameraInstance.this);
            m_camera.setPreviewStartListener(new CameraEx.PreviewStartListener() {
                @Override
                public void onStart(CameraEx cameraEx) {
                    Log.d(TAG, "Preview onStart");
                }
            });

            m_camera.setAutoPictureReviewControl(getAutoPictureReviewControls());
            m_camera.setPreviewAnalizeListener(CameraInstance.this);
            m_camera.setAutoISOSensitivityListener(CameraInstance.this);
            m_camera.setShutterListener(CameraInstance.this);
            m_camera.setApertureChangeListener(CameraInstance.this);
            m_camera.setProgramLineRangeOverListener(CameraInstance.this);
            m_camera.setPreviewMagnificationListener(CameraInstance.this);
            m_camera.setFocusDriveListener(CameraInstance.this);
            m_camera.setShutterSpeedChangeListener(CameraInstance.this);

            fireOnCameraOpen(true);

            if (surfaceHolder != null && isSurfaceCreated) {
                setSurfaceHolder(surfaceHolder);
                startPreview();
                startDisplay();
            }

        }
    };

    private void dumpParameter() {
        StringTokenizer localStringTokenizer = new StringTokenizer(((Camera.Parameters)parameters).flatten(), ";");
        while (localStringTokenizer.hasMoreElements())
            Log.d(TAG, localStringTokenizer.nextToken());

        List<String> tmp = null;
        if (isImageStabSupported())
        {
            Log.d(TAG,"getSupportedImageStabModes");
            tmp = getSupportedImageStabModes();
            logList(tmp);
        }
        if (isLiveSlowShutterSupported()) {
            Log.d(TAG, "getSupportedLiveSlowShutterModes");
            tmp = getSupportedLiveSlowShutterModes();
            logList(tmp);
        }
    }

    private void logList(List<String> list)
    {
        String st = new String();
        for (String s : list)
            st += s+",";
        Log.d(TAG,st);
    }

    public void closeCamera() {
        Log.d(TAG, "closeCamera");
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

    public void takePicture() {
        sendMsgToCam(CAPTURE_IMAGE);

    }

    public void cancelTakePicture() {
        sendMsgToCam(CANCEL_CAPTURE);

    }
    

    public void setSurfaceHolder(SurfaceHolder surface) {
        Log.d(TAG, "setSurfaceHolder");
        try {
            m_camera.getNormalCamera().setPreviewDisplay(surface);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startDisplay() {
        sendMsgToCam(START_DISPLAY);
    }

    public void stopDisplay() {
        sendMsgToCam(STOP_DISPLAY);

    }

    public void startPreview() {
        sendMsgToCam(START_PREVIEW);

    }

    public void stopPreview() {
       sendMsgToCam(STOP_PREVIEW);
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;
        if (m_camera != null) {
            setSurfaceHolder(surfaceHolder);
            startDisplay();
        }
        isSurfaceCreated = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        isSurfaceCreated = false;
        this.surfaceHolder = null;
    }


    private class BackGroundHandler extends Handler {

        public BackGroundHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
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
                case SET_ISO:
                    modifier.setISOSensitivity(msg.arg1);
                    setParameters(parameters);
                    break;
                case SET_EV:
                    parameters.setExposureCompensation(msg.arg1);
                    setParameters(parameters);
                    break;
                case SET_AUTO_SHUTTER_SPEED_LOW_LIMIT:
                    modifier.setAutoShutterSpeedLowLimit(msg.arg1);
                    setParameters(parameters);
                    break;
                case SET_SELF_TIMER:
                    modifier.setSelfTimer(msg.arg1);
                    setParameters(parameters);
                    break;
                case SET_PREVIEWMAGNIFICATION:
                    m_camera.setPreviewMagnification(msg.arg1,(Pair)msg.obj);
                    break;
                case SET_ADJUST_SHUTTER_SPEED:
                    m_camera.adjustShutterSpeed(msg.arg1);
                    break;
                case START_DISPLAY:
                    m_camera.getNormalCamera().startPreview();
                    break;
                case STOP_DISPLAY:
                    m_camera.getNormalCamera().stopPreview();
                    break;
                case CAPTURE_IMAGE:
                    m_camera.stopDirectShutter(new CameraEx.DirectShutterStoppedCallback() {
                        @Override
                        public void onShutterStopped(CameraEx cameraEx) {
                            m_camera.burstableTakePicture();
                        }
                    });
                    break;
                case CANCEL_CAPTURE:
                    m_camera.cancelTakePicture();
                    m_camera.startDirectShutter();
                    break;
                case START_PREVIEW:
                    m_camera.startDirectShutter();
                    break;
                case STOP_PREVIEW:
                    m_camera.stopDirectShutter(null);
                    break;
                default:
                    super.handleMessage(msg);
            }

        }
    }

    private class MainHandler extends Handler {
        public MainHandler(Looper mainLooper) {
            super(mainLooper);
        }

        @Override
        public void handleMessage(Message msg) {

            synchronized (locker) {
                switch (msg.what) {
                    case MSG_PREVIEWANALIZELISTNER:
                        if (previewAnalizeListener != null)
                            previewAnalizeListener.onAnalizedData((CameraEx.AnalizedData) msg.obj, null);
                        break;
                    case MSG_AUTO_ISO_SENSITIVY_LISTNER:
                        if (autoISOSensitivityListener != null)
                            autoISOSensitivityListener.onChanged(msg.arg1, null);
                        break;
                    case MSG_SHUTTERSPEEDCHANGEDLISTNER:
                        if (shutterSpeedChangeListener != null)
                            shutterSpeedChangeListener.onShutterSpeedChange((CameraEx.ShutterSpeedInfo) msg.obj, null);
                        break;
                    case MSG_SHUTTERLISTNER:
                        if (shutterListener != null)
                            shutterListener.onShutter(msg.arg1, null);
                        break;
                    case MSG_APERTURECHANGEDLISTNER:
                        if (apertureChangeListener != null)
                            apertureChangeListener.onApertureChange((CameraEx.ApertureInfo) msg.obj, null);
                        break;
                    case MSG_PROGRAM_LINE_RANGE_OVER_LISTNER_EV:
                        if (programLineRangeOverListener != null) {
                            programLineRangeOverListener.onEVRange(msg.arg1, null);
                        }
                        break;
                    case MSG_FOCUS_DRIVE_LISTNER:
                        if (focusDriveListener != null)
                            focusDriveListener.onChanged((CameraEx.FocusPosition) msg.obj, null);
                        break;
                    case MSG_PREVIEW_MAGNIFICATION_LISTNER_CHANGED:
                        if (previewMagnificationListener != null) {
                            PreviewMagnificationHelper helper = (PreviewMagnificationHelper) msg.obj;
                            previewMagnificationListener.onChanged(helper.enable, helper.magFactor, helper.magLevel, helper.coordinates, null);
                        }
                        break;
                    case MSG_AUTO_FOCUS_START_LISTNER:
                        if (autoFocusStartListener != null)
                            autoFocusStartListener.onStart(null);
                        break;
                    case MSG_AUTO_FOCUS_STOP_LISTNER:
                        if (autoFocusDoneListener != null)
                            autoFocusDoneListener.onDone(msg.arg1,(int[])msg.obj,null);
                        break;

                    case CAMERAOPEN:
                        if (cameraEventsListner != null) {
                            if (msg.arg1 == 0)
                                cameraEventsListner.onCameraOpen(false);
                            else
                                cameraEventsListner.onCameraOpen(true);
                        }
                        break;
                    default:
                        super.handleMessage(msg);
                        break;

                }
            }
        }
    }
}

