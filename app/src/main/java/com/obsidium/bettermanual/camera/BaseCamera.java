package com.obsidium.bettermanual.camera;

import android.hardware.Camera;
import android.util.Log;
import android.util.Pair;

import com.sony.scalar.hardware.CameraEx;

import java.util.List;

/**
 * Created by KillerInk on 30.08.2017.
 */

public class BaseCamera implements CameraEventListenerInterface, CameraParameterInterface {

    private static final String TAG = BaseCamera.class.getSimpleName();

    CameraEx m_camera;
    CameraEvents m_cameraEvents;

    CameraEx.AutoPictureReviewControl autoPictureReviewControl;
    CameraEx.ShutterSpeedInfo shutterSpeedInfo;
    CameraEx.PreviewAnalizeListener previewAnalyzeListener;
    CameraEx.FocusDriveListener focusDriveListener;
    CameraEx.PreviewMagnificationListener previewMagnificationListener;
    CameraEx.FocusLightStateListener focusLightStateListener;
    CameraEx.SettingChangedListener settingChangedListener;

    protected boolean cameraIsOpen = false;

    public CameraEx getCameraEx() {
        return m_camera;
    }

    public interface CameraEvents {
        void onCameraOpen(boolean isOpen);
    }

    @Override
    public void fireOnCameraOpen(boolean isOpen) {
        if (m_cameraEvents != null) {
            m_cameraEvents.onCameraOpen(true);
        }
    }

    public CameraEx.AutoPictureReviewControl getAutoPictureReviewControls() {
        return autoPictureReviewControl;
    }

    @Override
    public void setPreviewAnalyzeListener(CameraEx.PreviewAnalizeListener previewAnalyzeListener) {
        this.previewAnalyzeListener = previewAnalyzeListener;
    }

    @Override
    public void setFocusDriveListener(CameraEx.FocusDriveListener focusDriveListener) {
        this.focusDriveListener = focusDriveListener;
    }

    @Override
    public void setPreviewMagnificationListener(CameraEx.PreviewMagnificationListener previewMagnificationListener) {
        this.previewMagnificationListener = previewMagnificationListener;
        m_camera.setPreviewMagnificationListener(previewMagnificationListener);
    }

    @Override
    public void setM_cameraEvents(CameraEvents eventsListener) {
        this.m_cameraEvents = eventsListener;
    }

    @Override
    public void setShutterListener(CameraEx.ShutterListener shutterListener) {
        m_camera.setShutterListener(shutterListener);
    }

    @Override
    public CameraEx.ShutterSpeedInfo getShutterSpeedInfo() {
        if (shutterSpeedInfo == null) {
            shutterSpeedInfo = new CameraEx.ShutterSpeedInfo();
            CameraEx.ParametersModifier modifier = m_camera.createParametersModifier(getParameters());
            Pair<Integer, Integer> p = modifier.getShutterSpeed();
            shutterSpeedInfo.currentShutterSpeed_d = p.first;
            shutterSpeedInfo.currentShutterSpeed_n = p.second;

        }
        return shutterSpeedInfo;
    }

    protected Camera.Parameters getParameters() {
        return m_camera.getNormalCamera().getParameters();
    }

    protected void setParameters(Camera.Parameters parameters) {
        m_camera.getNormalCamera().setParameters(parameters);
    }

    protected CameraEx.ParametersModifier getModifier() {
        return m_camera.createParametersModifier(getParameters());
    }

    protected Camera.Parameters getEmptyParameters() {
        return m_camera.createEmptyParameters();
    }

    public int getExposureCompensation() {
        return getParameters().getExposureCompensation();
    }

    public void setExposureCompensation(int value) {

        Camera.Parameters parameters = getEmptyParameters();
        parameters.setExposureCompensation(value);
        setParameters(parameters);
    }

    public int getMaxExposureCompensation() {
        return getParameters().getMaxExposureCompensation();
    }

    public int getMinExposureCompensation() {
        return getParameters().getMinExposureCompensation();
    }

    public float getExposureCompensationStep() {
        float ret = 0;
        try {
            ret = getParameters().getExposureCompensationStep();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
        return ret;
    }

    public boolean isLongExposureNoiseReductionSupported() {
        try {
            getModifier().getLongExposureNR();
            return true;
        } catch (NoSuchMethodError ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public void setLongExposureNoiseReduction(boolean enable) {
        Camera.Parameters parameters = getEmptyParameters();
        CameraEx.ParametersModifier modifier = getCameraEx().createParametersModifier(parameters);
        modifier.setLongExposureNR(enable);
        setParameters(parameters);
    }

    @Override
    public boolean getLongeExposureNR() {
        return getModifier().getLongExposureNR();
    }

    public void setFocusMode(String value) {
        Log.d(TAG, "setFocusmode:" + value);
        Camera.Parameters parameters = getEmptyParameters();
        parameters.setFocusMode(value);
        setParameters(parameters);
    }

    public String getSceneMode() {
        return getParameters().getSceneMode();
    }

    public void setSceneMode(String value) {
        Log.d(TAG, "setSceneMode:" + value);
        Camera.Parameters parameters = getEmptyParameters();
        parameters.setSceneMode(value);
        setParameters(parameters);
    }

    public String getDriveMode() {
        return getModifier().getDriveMode();
    }

    public void setDriveMode(String value) {
        Log.d(TAG, "setDriveMode:" + value);
        Camera.Parameters parameters = getEmptyParameters();
        CameraEx.ParametersModifier modifier = getCameraEx().createParametersModifier(parameters);
        modifier.setDriveMode(value);
        setParameters(parameters);
    }

    public void setImageAspectRatio(String value) {
        Camera.Parameters parameters = getEmptyParameters();
        CameraEx.ParametersModifier modifier = getCameraEx().createParametersModifier(parameters);
        modifier.setImageAspectRatio(value);
        setParameters(parameters);
    }

    public void setImageQuality(String value) {
        Camera.Parameters parameters = getEmptyParameters();
        CameraEx.ParametersModifier modifier = m_camera.createParametersModifier(parameters);
        modifier.setPictureStorageFormat(value);
        setParameters(parameters);
    }

    public String getBurstDriveSpeed() {
        return getModifier().getBurstDriveSpeed();
    }

    public void setBurstDriveSpeed(String value) {
        Camera.Parameters parameters = getEmptyParameters();
        CameraEx.ParametersModifier modifier = m_camera.createParametersModifier(parameters);
        modifier.setBurstDriveSpeed(value);
        setParameters(parameters);
    }

    public boolean isAutoShutterSpeedLowLimitSupported() {
        return getModifier().isSupportedAutoShutterSpeedLowLimit();
    }

    public int getAutoShutterSpeedLowLimit() {
        return getModifier().getAutoShutterSpeedLowLimit();
    }

    public void setAutoShutterSpeedLowLimit(int value) {
        Camera.Parameters parameters = getEmptyParameters();
        CameraEx.ParametersModifier modifier = m_camera.createParametersModifier(parameters);
        modifier.setAutoShutterSpeedLowLimit(value);
        setParameters(parameters);
    }

    public void setSelfTimer(int value) {
        Camera.Parameters parameters = getEmptyParameters();
        CameraEx.ParametersModifier modifier = m_camera.createParametersModifier(parameters);
        modifier.setSelfTimer(value);
        setParameters(parameters);
    }

    public List<Integer> getSupportedISOSensitivities() {
        return getModifier().getSupportedISOSensitivities();
    }

    public int getISOSensitivity() {
        return getModifier().getISOSensitivity();
    }

    public void setISOSensitivity(int value) {
        Camera.Parameters parameters = getEmptyParameters();
        CameraEx.ParametersModifier modifier = m_camera.createParametersModifier(parameters);
        modifier.setISOSensitivity(value);
        setParameters(parameters);
    }

    public void setPreviewMagnification(int factor, Pair position) {
        m_camera.setPreviewMagnification(factor, position);
    }

    @Override
    public void stopPreviewMagnification() {
        m_camera.stopPreviewMagnification();
    }

    public List<Integer> getSupportedPreviewMagnification() {
        return getModifier().getSupportedPreviewMagnification();
    }

    public void decrementShutterSpeed() {
        m_camera.decrementShutterSpeed();
    }

    public void incrementShutterSpeed() {
        m_camera.incrementShutterSpeed();
    }

    public void decrementAperture() {
        m_camera.decrementAperture();
    }

    public void incrementAperture() {
        m_camera.incrementAperture();
    }

    public int getAperture() {
        return getModifier().getAperture();
    }

    @Override
    public boolean isImageStabSupported() {
        try {
            getModifier().getAntiHandBlurMode();

            return true;
        } catch (NoSuchMethodError ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public String getImageStabilisationMode() {
        return getModifier().getAntiHandBlurMode();
    }

    @Override
    public void setImageStabilisation(String enable) {
        Camera.Parameters parameters = getEmptyParameters();
        CameraEx.ParametersModifier modifier = m_camera.createParametersModifier(parameters);
        modifier.setAntiHandBlurMode(enable);
        setParameters(parameters);
    }

    @Override
    public List<String> getSupportedImageStabModes() {

        return getModifier().getSupportedAntiHandBlurModes();
    }

    @Override
    public boolean isLiveSlowShutterSupported() {
        try {
            return getModifier().isSupportedSlowShutterLiveviewMode();
        } catch (NoSuchMethodError ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public String getLiveSlowShutter() {
        return getModifier().getSlowShutterLiveviewMode();
    }

    public void setLiveSlowShutter(String liveSlowShutter) {
        Camera.Parameters parameters = getEmptyParameters();
        CameraEx.ParametersModifier modifier = m_camera.createParametersModifier(parameters);
        modifier.setSlowShutterLiveviewMode(liveSlowShutter);
        setParameters(parameters);
    }

    @Override
    public String[] getSupportedLiveSlowShutterModes() {
        return new String[]{getModifier().SLOW_SHUTTER_LIVEVIEW_MODE_OFF, getModifier().SLOW_SHUTTER_LIVEVIEW_MODE_ON};
    }

    public Pair getShutterSpeed() {
        return getModifier().getShutterSpeed();
    }

    public void adjustShutterSpeed(int val) {
        m_camera.adjustShutterSpeed(val);
    }

    public void setFocusPosition(int pos) {
        if (pos < 0)
            m_camera.startOneShotFocusDrive(CameraEx.FOCUS_DRIVE_DIRECTION_NEAR, pos * -1);
        else
            m_camera.startOneShotFocusDrive(CameraEx.FOCUS_DRIVE_DIRECTION_FAR, pos);
    }

    public void setRedEyeReduction(String enable) {
        Camera.Parameters parameters = getEmptyParameters();
        CameraEx.ParametersModifier modifier = m_camera.createParametersModifier(parameters);
        modifier.setRedEyeReductionMode(enable);
        setParameters(parameters);
    }

    public void setFlashMode(String enable) {
        Camera.Parameters parameters = getEmptyParameters();
        parameters.setFlashMode(enable);
        setParameters(parameters);
    }

    public void setFlashType(String enable) {
        Camera.Parameters parameters = getEmptyParameters();
        CameraEx.ParametersModifier modifier = m_camera.createParametersModifier(parameters);
        modifier.setFlashType(enable);
        setParameters(parameters);
    }



    //returns always [0,0,0] when used with mf, dont know if its works with af
    /*public float[]getFocusDistances()
    {
        Camera.Parameters parameters = m_camera.getNormalCamera().getParameters();
        float ar[] = new float[3];
        parameters.getFocusDistances(ar);
        return ar;
    }*/
}
