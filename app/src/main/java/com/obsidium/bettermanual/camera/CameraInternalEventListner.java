package com.obsidium.bettermanual.camera;

import android.hardware.Camera;
import android.util.Pair;

import com.sony.scalar.hardware.CameraEx;
import com.sony.scalar.meta.FaceInfo;

/**
 * Created by KillerInk on 29.09.2017.
 */

public class CameraInternalEventListner extends BaseCamera implements CameraEx.ApertureChangeListener, CameraEx.ShutterSpeedChangeListener,
        CameraEx.ProgramLineRangeOverListener, CameraEx.FocusLightStateListener, CameraEx.AutoISOSensitivityListener,
        CameraEx.SettingChangedListener, CameraEx.EquipmentCallback, CameraEx.FlashChargingStateListener,CameraEx.FlashEmittingListener,CameraEx.ProgramLineListener,
        CameraEx.ErrorCallback,CameraEx.AutoSceneModeListener,CameraEx.FaceDetectionListener, CameraEx.FocalLengthChangeListener,
        CameraEx.PowerZoomListener, CameraEx.ZoomChangeListener, CameraEx.FocusAreaListener, CameraEx.PreviewMagnificationListener, CameraEx.AutoApscModeListener,
        CameraEx.FocusDriveListener, CameraEx.ShutterListener
{


    protected void initListners()
    {
        m_camera.setApertureChangeListener(this);
        m_camera.setShutterSpeedChangeListener(this);
        m_camera.setProgramLineRangeOverListener(this);
        m_camera.setFocusLightStateListener(this);
        m_camera.setAutoISOSensitivityListener(this);
        m_camera.setSettingChangedListener(this);

        m_camera.setEquipmentCallback(this);
        m_camera.setFlashChargingStateListener(this);
        m_camera.setFlashEmittingListener(this);
        m_camera.setProgramLineListener(this);
        m_camera.setErrorCallback(this);
        m_camera.setAutoSceneModeListener(this);
        m_camera.setFaceDetectionListener(this);

        if (modifier.isSupportedFocalLengthNotify())
            m_camera.setFocalLengthChangeListener(this);
        m_camera.setPowerZoomListener(this);
        m_camera.setZoomChangeListener(this);
        m_camera.setShutterListener(this);

        m_camera.setFocusAreaListener(this);
        m_camera.setPreviewMagnificationListener(this);
        m_camera.setAutoApscModeListener(this);
        m_camera.setFocusDriveListener(this);
    }

    protected void removeListners()
    {
        m_camera.setApertureChangeListener(null);
        m_camera.setShutterSpeedChangeListener(null);
        m_camera.setProgramLineRangeOverListener(null);
        m_camera.setFocusLightStateListener(null);
        m_camera.setAutoISOSensitivityListener(null);
        m_camera.setSettingChangedListener(null);

        m_camera.setEquipmentCallback(null);
        m_camera.setFlashChargingStateListener(null);
        m_camera.setFlashEmittingListener(null);
        m_camera.setProgramLineListener(null);
        m_camera.setErrorCallback(null);
        m_camera.setAutoSceneModeListener(null);
        m_camera.setFaceDetectionListener(null);

        if (modifier.isSupportedFocalLengthNotify())
            m_camera.setFocalLengthChangeListener(null);
        m_camera.setPowerZoomListener(null);
        m_camera.setZoomChangeListener(null);
        m_camera.setShutterListener(null);

        m_camera.setFocusAreaListener(null);
        m_camera.setPreviewMagnificationListener(null);
        m_camera.setAutoApscModeListener(null);
        m_camera.setFocusDriveListener(null);
    }

    @Override
    public void onApertureChange(CameraEx.ApertureInfo apertureInfo, CameraEx cameraEx) {
        if (apertureChangeListener != null)
            apertureChangeListener.onApertureChange(apertureInfo,cameraEx);
    }

    @Override
    public void onShutterSpeedChange(CameraEx.ShutterSpeedInfo shutterSpeedInfo, CameraEx cameraEx) {
        this.shutterSpeedInfo = shutterSpeedInfo;
        if (shutterSpeedChangeListener != null)
            shutterSpeedChangeListener.onShutterSpeedChange(shutterSpeedInfo,cameraEx);
    }

    @Override
    public void onAERange(boolean b, boolean b1, boolean b2, CameraEx cameraEx) {
        if (programLineRangeOverListener != null)
            programLineRangeOverListener.onAERange(b,b1,b2,cameraEx);
    }

    @Override
    public void onEVRange(int i, CameraEx cameraEx) {
        if (programLineRangeOverListener != null)
            programLineRangeOverListener.onEVRange(i,cameraEx);
    }

    @Override
    public void onMeteringRange(boolean b, CameraEx cameraEx) {
        if (programLineRangeOverListener != null)
            programLineRangeOverListener.onMeteringRange(b,cameraEx);
    }

    @Override
    public void onChanged(boolean b, boolean b1, CameraEx cameraEx) {
        if (focusLightStateListener != null)
            focusLightStateListener.onChanged(b,b1,cameraEx);
    }

    @Override
    public void onChanged(int i, CameraEx cameraEx) {
        if (autoISOSensitivityListener != null)
            autoISOSensitivityListener.onChanged(i,cameraEx);
    }

    @Override
    public void onChanged(int[] ints, Camera.Parameters parameters, CameraEx cameraEx) {
        if (settingChangedListener !=null)
            settingChangedListener.onChanged(ints,parameters,cameraEx);
    }

    @Override
    public void onEquipmentChange(int i, int i1, CameraEx cameraEx) {

    }

    //AutoSceneModeListner
    @Override
    public void onChanged(String s, CameraEx cameraEx) {

    }

    //Error Callback
    @Override
    public void onError(int i, CameraEx cameraEx) {

    }

    //FaceDetectionListner
    @Override
    public void onFaceDetected(FaceInfo[] faceInfos, CameraEx cameraEx) {

    }

    //FlashEmittingListner
    @Override
    public void onFlash(boolean b, CameraEx cameraEx) {

    }

    //ProgramLineListner
    @Override
    public void onChanged(boolean b, CameraEx cameraEx) {

    }

    @Override
    public void onFocalLengthChanged(int i, CameraEx cameraEx) {

    }

    //zoom changed listner
    @Override
    public void onChanged(CameraEx.ZoomInfo zoomInfo, CameraEx cameraEx) {

    }

    //Focus Area Listner
    @Override
    public void onChanged(CameraEx.FocusAreaInfos focusAreaInfos, CameraEx cameraEx) {

    }

    @Override
    public void onChanged(boolean b, int i, int i1, Pair pair, CameraEx cameraEx) {
        if (previewMagnificationListener != null)
            previewMagnificationListener.onChanged(b,i,i1,pair,cameraEx);
    }

    @Override
    public void onInfoUpdated(boolean b, Pair pair, CameraEx cameraEx) {
        if (previewMagnificationListener != null)
            previewMagnificationListener.onInfoUpdated(b,pair,cameraEx);
    }

    //FocusDriveListner
    @Override
    public void onChanged(CameraEx.FocusPosition focusPosition, CameraEx cameraEx) {
        if (focusDriveListener !=null)
            focusDriveListener.onChanged(focusPosition,cameraEx);
    }

    @Override
    public void onShutter(int i, CameraEx cameraEx) {
        if (shutterListener != null)
            shutterListener.onShutter(i,cameraEx);
    }
}
