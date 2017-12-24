package com.obsidium.bettermanual.camera;

import android.hardware.Camera;
import android.util.Pair;

import com.sony.scalar.hardware.CameraEx;

import java.util.List;

/**
 * Created by KillerInk on 30.08.2017.
 */

public class BaseCamera implements CameraEventListnerInterface, CameraParameterInterface {



    public interface CameraEvents{
        void onCameraOpen(boolean isOpen);
    }

    protected CameraHandler cameraHandler;


    CameraEx.AutoPictureReviewControl autoPictureReviewControl;
    CameraEvents cameraEventsListner;
    CameraEx.ShutterSpeedInfo shutterSpeedInfo;
    CameraEx m_camera;
    protected boolean cameraIsOpen = false;

    CameraEx.PreviewAnalizeListener previewAnalizeListener;
    CameraEx.AutoISOSensitivityListener autoISOSensitivityListener;
    CameraEx.ShutterSpeedChangeListener shutterSpeedChangeListener;
    CameraEx.ApertureChangeListener apertureChangeListener;
    CameraEx.ProgramLineRangeOverListener programLineRangeOverListener;
    CameraEx.FocusDriveListener focusDriveListener;
    CameraEx.PreviewMagnificationListener previewMagnificationListener;
    CameraEx.AutoFocusStartListener autoFocusStartListener;
    CameraEx.AutoFocusDoneListener autoFocusDoneListener;
    CameraEx.ShutterListener shutterListener;
    CameraEx.FocusLightStateListener focusLightStateListener;
    CameraEx.SettingChangedListener settingChangedListener;

    public CameraEx getCameraEx()
    {
        return m_camera;
    }


    public CameraEx.AutoPictureReviewControl getAutoPictureReviewControls()
    {
        return autoPictureReviewControl;
    }

    @Override
    public void setPreviewAnalizeListener(CameraEx.PreviewAnalizeListener previewAnalizeListener)
    {
        this.previewAnalizeListener = previewAnalizeListener;
    }

    @Override
    public void setAutoISOSensitivityListener(CameraEx.AutoISOSensitivityListener autoISOSensitivityListener)
    {
        this.autoISOSensitivityListener = autoISOSensitivityListener;
    }

    @Override
    public void setShutterSpeedChangeListener(CameraEx.ShutterSpeedChangeListener shutterSpeedChangeListener)
    {
        this.shutterSpeedChangeListener = shutterSpeedChangeListener;
    }


    @Override
    public void setApertureChangeListener(CameraEx.ApertureChangeListener apertureChangeListener)
    {
        this.apertureChangeListener = apertureChangeListener;
    }

    @Override
    public void setProgramLineRangeOverListener(CameraEx.ProgramLineRangeOverListener programLineRangeOverListener)
    {
       this.programLineRangeOverListener = programLineRangeOverListener;
    }

    @Override
    public void setFocusDriveListener(CameraEx.FocusDriveListener focusDriveListener)
    {
       this.focusDriveListener = focusDriveListener;
    }

    @Override
    public void setPreviewMagnificationListener(CameraEx.PreviewMagnificationListener previewMagnificationListener)
    {
        this.previewMagnificationListener = previewMagnificationListener;
    }

    @Override
    public void setAutoFocusStartListener(CameraEx.AutoFocusStartListener autoFocusStartListener)
    {
        this.autoFocusStartListener = autoFocusStartListener;
    }

    @Override
    public void setAutoFocusDoneListener(CameraEx.AutoFocusDoneListener autoFocusDoneListener)
    {
        this.autoFocusDoneListener = autoFocusDoneListener;
    }

    @Override
    public void setCameraEventsListner(CameraEvents eventsListner)
    {
        this.cameraEventsListner = eventsListner;
    }

    @Override
    public void fireOnCameraOpen(boolean isopen)
    {
        if (cameraEventsListner != null)
        {
            cameraEventsListner.onCameraOpen(true);
        }
    }

    @Override
    public void setShutterListener(CameraEx.ShutterListener shutterListener) {
        this.shutterListener = shutterListener;
    }

    @Override
    public void setFocusLightStateListner(CameraEx.FocusLightStateListener focusLightStateListner) {
        this.focusLightStateListener = focusLightStateListner;
    }

    @Override
    public void setSettingsChangedListner(CameraEx.SettingChangedListener settingsChangedListner) {
        this.settingChangedListener = settingsChangedListner;
    }

    @Override
    public CameraEx.ShutterSpeedInfo getShutterSpeedInfo()
    {
        if (shutterSpeedInfo == null) {
            shutterSpeedInfo = new CameraEx.ShutterSpeedInfo();
            CameraEx.ParametersModifier modifier = m_camera.createParametersModifier(getParameters());
            Pair<Integer, Integer> p = modifier.getShutterSpeed();
            shutterSpeedInfo.currentShutterSpeed_d = p.first;
            shutterSpeedInfo.currentShutterSpeed_n = p.second;

        }
        return shutterSpeedInfo;
    }

    protected Camera.Parameters getParameters()
    {
        return m_camera.getNormalCamera().getParameters();
    }

    protected CameraEx.ParametersModifier getModifier()
    {
        return m_camera.createParametersModifier(getParameters());
    }


    protected Camera.Parameters getEmptyParameters()
    {
        return m_camera.createEmptyParameters();
    }



    protected void setParameters(Camera.Parameters parameters)
    {
        m_camera.getNormalCamera().setParameters(parameters);
    }

    public int getExposureCompensation() {
        return getParameters().getExposureCompensation();
    }

    public void setExposureCompensation(int value) {
        cameraHandler.sendMessage(cameraHandler.obtainMessage(CameraInstance.SET_EV,value,0));
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
        }
        catch (NullPointerException ex)
        {
            ex.printStackTrace();
        }
        return ret;
    }

    public boolean isLongExposureNoiseReductionSupported()
    {
        try {
            getModifier().getLongExposureNR();
            return true;
        }
        catch (NoSuchMethodError ex)
        {
            ex.printStackTrace();
            return false;
        }
    }

    public void setLongExposureNoiseReduction(boolean enable)
    {
        cameraHandler.sendMessage(cameraHandler.obtainMessage(MSG_SET_LONGEXPONR,enable));
    }

    @Override
    public boolean getLongeExposureNR() {
        return getModifier().getLongExposureNR();
    }

    public void setFocusMode(String value)
    {
        cameraHandler.sendMessage(cameraHandler.obtainMessage(MSG_SET_FOCUSMODE,value));
    }

    public void setSceneMode(String value)
    {
        cameraHandler.sendMessage(cameraHandler.obtainMessage(MSG_SET_SCENEMODE,value));
    }

    public String getSceneMode()
    {
        return getParameters().getSceneMode();
    }

    public void setDriveMode(String value)
    {
        cameraHandler.sendMessage(cameraHandler.obtainMessage(MSG_SET_DRIVEMODE,value));
    }

    public String getDriveMode()
    {
        return getModifier().getDriveMode();
    }

    public void setImageAspectRatio(String value)
    {
        cameraHandler.sendMessage(cameraHandler.obtainMessage(MSG_SET_IMAGEASPECTRATIO,value));

    }

    public void setImageQuality(String value)
    {
        cameraHandler.sendMessage(cameraHandler.obtainMessage(MSG_SET_IMAGEQUALITY,value));
    }

    public void setBurstDriveSpeed(String value)
    {
        cameraHandler.sendMessage(cameraHandler.obtainMessage(MSG_SET_BURSTDRIVESPEED,value));
    }

    public String getBurstDriveSpeed()
    {
        return getModifier().getBurstDriveSpeed();
    }

    public boolean isAutoShutterSpeedLowLimitSupported()
    {
        return getModifier().isSupportedAutoShutterSpeedLowLimit();
    }

    public void setAutoShutterSpeedLowLimit(int value)
    {
        cameraHandler.sendMessage(cameraHandler.obtainMessage(SET_AUTO_SHUTTER_SPEED_LOW_LIMIT,value,0));
    }

    public int getAutoShutterSpeedLowLimit()
    {
        return getModifier().getAutoShutterSpeedLowLimit();
    }

    public void setSelfTimer(int value)
    {
        cameraHandler.sendMessage(cameraHandler.obtainMessage(SET_SELF_TIMER,value,0));
    }

    public List<Integer> getSupportedISOSensitivities()
    {
        return getModifier().getSupportedISOSensitivities();
    }

    public int getISOSensitivity()
    {
        return getModifier().getISOSensitivity();
    }

    public void setISOSensitivity(int value)
    {
        cameraHandler.sendMessage(cameraHandler.obtainMessage(SET_ISO,value,0));
    }

    public void setPreviewMagnification(int factor, Pair position)
    {
        m_camera.setPreviewMagnification(factor, position);
    }

    @Override
    public void stopPreviewMagnification() {
        m_camera.stopPreviewMagnification();
    }

    public List<Integer> getSupportedPreviewMagnification() {
        return getModifier().getSupportedPreviewMagnification();
    }

    public void decrementShutterSpeed(){
        cameraHandler.sendMessage(cameraHandler.obtainMessage(DECREASE_SHUTTER));
    }
    public void incrementShutterSpeed()
    {
        cameraHandler.sendMessage(cameraHandler.obtainMessage(INCREASE_SHUTTER));
    }

    public void decrementAperture(){
        cameraHandler.sendMessage(cameraHandler.obtainMessage(DECREASE_APERTURE));
    }

    public void incrementAperture(){
        cameraHandler.sendMessage(cameraHandler.obtainMessage(INCREASE_APERTURE));
    }


    public int getAperture() {
        return getModifier().getAperture();
    }

    @Override
    public boolean isImageStabSupported() {
        try {
            getModifier().getAntiHandBlurMode();

            return true;
        }
        catch (NoSuchMethodError ex) {
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
        cameraHandler.sendMessage(cameraHandler.obtainMessage(MSG_SET_IMAGESTABILISATION,enable));
    }

    @Override
    public List<String> getSupportedImageStabModes() {

        return getModifier().getSupportedAntiHandBlurModes();
    }

    @Override
    public boolean isLiveSlowShutterSupported() {
        try {
            return getModifier().isSupportedSlowShutterLiveviewMode();
        }
        catch (NoSuchMethodError ex)
        {
            ex.printStackTrace();
            return false;
        }
    }

    public void setLiveSlowShutter(String liveSlowShutter)
    {
        Camera.Parameters parameters = getEmptyParameters();
        CameraEx.ParametersModifier modifier = m_camera.createParametersModifier(parameters);
        modifier.setSlowShutterLiveviewMode(liveSlowShutter);
        setParameters(parameters);
    }

    @Override
    public String getLiveSlowShutter() {
        return getModifier().getSlowShutterLiveviewMode();
    }

    @Override
    public String[] getSupportedLiveSlowShutterModes() {
        return new String[] { getModifier().SLOW_SHUTTER_LIVEVIEW_MODE_OFF,getModifier().SLOW_SHUTTER_LIVEVIEW_MODE_ON};
    }



    public Pair getShutterSpeed()
    {
        return getModifier().getShutterSpeed();
    }

    public void adjustShutterSpeed(int val)
    {
        m_camera.adjustShutterSpeed(val);
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
