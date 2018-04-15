package com.obsidium.bettermanual.camera;

import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.sony.scalar.hardware.CameraEx;

import java.lang.ref.WeakReference;

/**
 * Created by KillerInk on 23.12.2017.
 */

public class CameraHandler extends Handler
{

    WeakReference<CameraInstance> cameraInstanceWeakReference;
    public CameraHandler(Looper looper, CameraInstance cameraEx)
    {
        super(looper);
        cameraInstanceWeakReference = new WeakReference<CameraInstance>(cameraEx);
    }

    @Override
    public void handleMessage(Message msg) {
        CameraInstance cameraEx = cameraInstanceWeakReference.get();
        if (cameraEx == null)
            return;
        switch (msg.what)
        {
            case CameraParameterInterface.MSG_INIT_CAMERA:
                cameraEx.initCamera();
                break;
            case CameraParameterInterface.CAPTURE_IMAGE:
                //hw shutter button must get stopped else burstableTakePicture does not trigger
                cameraEx.getCameraEx().stopDirectShutter(new CameraEx.DirectShutterStoppedCallback() {
                    @Override
                    public void onShutterStopped(CameraEx cameraEx) {
                        cameraEx.burstableTakePicture();
                    }
                });
                break;
            case CameraParameterInterface.CANCEL_CAPTURE:
                cameraEx.getCameraEx().cancelTakePicture();
                break;
            case CameraParameterInterface.START_PREVIEW:
                cameraEx.getCameraEx().getNormalCamera().startPreview();
                break;
            case CameraParameterInterface.STOP_PREVIEW:
                cameraEx.getCameraEx().getNormalCamera().stopPreview();
                break;
            case CameraParameterInterface.SET_EV:
                Camera.Parameters parameters = cameraEx.getEmptyParameters();
                parameters.setExposureCompensation(msg.arg1);
                cameraEx.setParameters(parameters);
                break;
            case CameraParameterInterface.MSG_SET_LONGEXPONR:
                parameters = cameraEx.getEmptyParameters();
                CameraEx.ParametersModifier modifier = cameraEx.getCameraEx().createParametersModifier(parameters);
                modifier.setLongExposureNR((Boolean) msg.obj);
                cameraEx.setParameters(parameters);
                break;
            case CameraParameterInterface.MSG_SET_FOCUSMODE:
                parameters = cameraEx.getEmptyParameters();
                parameters.setFocusMode((String)msg.obj);
                cameraEx.setParameters(parameters);
                break;
            case CameraParameterInterface.MSG_SET_SCENEMODE:
                parameters = cameraEx.getEmptyParameters();
                parameters.setSceneMode((String)msg.obj);
                cameraEx.setParameters(parameters);
                break;
            case CameraParameterInterface.MSG_SET_DRIVEMODE:
                parameters = cameraEx.getEmptyParameters();
                modifier = cameraEx.getCameraEx().createParametersModifier(parameters);
                modifier.setDriveMode((String)msg.obj);
                cameraEx.setParameters(parameters);
                break;
            case CameraParameterInterface.MSG_SET_IMAGEASPECTRATIO:
                parameters = cameraEx.getEmptyParameters();
                modifier = cameraEx.getCameraEx().createParametersModifier(parameters);
                modifier.setImageAspectRatio((String)msg.obj);
                cameraEx.setParameters(parameters);
                break;
            case CameraParameterInterface.MSG_SET_IMAGEQUALITY:
                parameters = cameraEx.getEmptyParameters();
                modifier = cameraEx.getCameraEx().createParametersModifier(parameters);
                modifier.setPictureStorageFormat((String)msg.obj);
                cameraEx.setParameters(parameters);
                break;
            case CameraParameterInterface.MSG_SET_BURSTDRIVESPEED:
                parameters = cameraEx.getEmptyParameters();
                modifier = cameraEx.m_camera.createParametersModifier(parameters);
                modifier.setBurstDriveSpeed((String)msg.obj);
                cameraEx.setParameters(parameters);
                break;
            case CameraParameterInterface.SET_AUTO_SHUTTER_SPEED_LOW_LIMIT:
                parameters = cameraEx.getEmptyParameters();
                modifier = cameraEx.m_camera.createParametersModifier(parameters);
                modifier.setAutoShutterSpeedLowLimit(msg.arg1);
                cameraEx.setParameters(parameters);
                break;
            case CameraParameterInterface.SET_SELF_TIMER:
                parameters = cameraEx.getEmptyParameters();
                modifier = cameraEx.m_camera.createParametersModifier(parameters);
                modifier.setSelfTimer(msg.arg1);
                cameraEx.setParameters(parameters);
                break;
            case CameraParameterInterface.SET_ISO:
                parameters = cameraEx.getEmptyParameters();
                modifier = cameraEx.m_camera.createParametersModifier(parameters);
                modifier.setISOSensitivity(msg.arg1);
                cameraEx.setParameters(parameters);
                break;
            case CameraParameterInterface.INCREASE_SHUTTER:
                cameraEx.m_camera.incrementShutterSpeed();
                break;
            case CameraParameterInterface.DECREASE_SHUTTER:
                cameraEx.m_camera.decrementShutterSpeed();
                break;
            case CameraParameterInterface.INCREASE_APERTURE:
                cameraEx.m_camera.incrementAperture();
                break;
            case CameraParameterInterface.DECREASE_APERTURE:
                cameraEx.m_camera.decrementAperture();
                break;
            case CameraParameterInterface.MSG_SET_IMAGESTABILISATION:
                parameters = cameraEx.getEmptyParameters();
                modifier = cameraEx.m_camera.createParametersModifier(parameters);
                modifier.setAntiHandBlurMode((String) msg.obj);
                cameraEx.setParameters(parameters);
                break;

            default:
                super.handleMessage(msg);
        }
    }
}
