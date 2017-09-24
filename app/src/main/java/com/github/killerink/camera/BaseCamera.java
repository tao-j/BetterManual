package com.github.killerink.camera;

import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
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

    class PreviewMagnificationHelper
    {
        boolean enable;
        int magFactor;
        int magLevel;
        Pair coordinates;
        public PreviewMagnificationHelper(boolean enable, int magFactor, int magLevel, Pair coordinates)
        {
            this.enable = enable;
            this.magFactor = magFactor;
            this.magLevel = magLevel;
            this.coordinates = coordinates;
        }

    }


    CameraEx.AutoPictureReviewControl autoPictureReviewControl;
    CameraEx.PreviewAnalizeListener previewAnalizeListener;

    CameraEx.AutoISOSensitivityListener autoISOSensitivityListener;

    CameraEx.ShutterSpeedChangeListener shutterSpeedChangeListener;

    CameraEx.ShutterListener shutterListener;
    CameraEx.ApertureChangeListener apertureChangeListener;
    CameraEx.ProgramLineRangeOverListener programLineRangeOverListener;
    CameraEx.FocusDriveListener focusDriveListener;
    CameraEx.PreviewMagnificationListener previewMagnificationListener;
    CameraEx.AutoFocusStartListener autoFocusStartListener;
    CameraEx.AutoFocusDoneListener autoFocusDoneListener;

    CameraInternalEventImpl.CameraEvents cameraEventsListner;

    final Object locker = new Object();


    CameraEx.ShutterSpeedInfo shutterSpeedInfo;

    Handler uiHandler;
    Handler cameraHandler;

    Camera.Parameters parameters;
    CameraEx.ParametersModifier modifier;
    CameraEx m_camera;
    protected boolean cameraIsOpen = false;

    public CaptureSession captureSession;

     void sendMsgToCam(int code)
    {
        sendMsg(cameraHandler,code);
    }

    void sendMsgToCam(int code, int value)
    {
        sendMsg(cameraHandler, code,value);
    }

    void sendMsgToUi(int code)
    {
        sendMsg(uiHandler,code);
    }

    void sendMsgToUi(int code, int value)
    {
        sendMsg(uiHandler, code,value);
    }

    void sendMsgToUi(int code, Object value)
    {
        sendMsg(uiHandler, code,value);
    }

    private void sendMsg(Handler handler, int code)
    {
        Message msg = handler.obtainMessage();
        msg.what = code;
        handler.sendMessage(msg);
    }

    private void sendMsg(Handler handler, int code, int value)
    {
        Message msg = handler.obtainMessage();
        msg.what = code;
        msg.arg1 = value;
        handler.sendMessage(msg);
    }
    private void sendMsg(Handler handler, int code, Object value)
    {
        Message msg = handler.obtainMessage();
        msg.what = code;
        msg.obj = value;
        handler.sendMessage(msg);
    }

    @Override
    public CameraEx.AutoPictureReviewControl getAutoPictureReviewControls()
    {
        synchronized (locker) {
            return autoPictureReviewControl;
        }
    }

    @Override
    public void setPreviewAnalizeListener(CameraEx.PreviewAnalizeListener previewAnalizeListener)
    {
        synchronized (locker) {
            this.previewAnalizeListener = previewAnalizeListener;
        }
    }

    @Override
    public void setAutoISOSensitivityListener(CameraEx.AutoISOSensitivityListener autoISOSensitivityListener)
    {
        synchronized (locker) {
            this.autoISOSensitivityListener = autoISOSensitivityListener;
        }
    }

    @Override
    public void setShutterSpeedChangeListener(CameraEx.ShutterSpeedChangeListener shutterSpeedChangeListener)
    {
        synchronized (locker) {
            this.shutterSpeedChangeListener = shutterSpeedChangeListener;
        }
    }

    @Override
    public void setShutterListener(CameraEx.ShutterListener shutterListener)
    {
        synchronized (locker) {
            this.shutterListener = shutterListener;
        }
    }

    @Override
    public void setApertureChangeListener(CameraEx.ApertureChangeListener apertureChangeListener)
    {
        synchronized (locker) {
            this.apertureChangeListener = apertureChangeListener;
        }
    }

    @Override
    public void setProgramLineRangeOverListener(CameraEx.ProgramLineRangeOverListener programLineRangeOverListener)
    {
        synchronized (locker) {
            this.programLineRangeOverListener = programLineRangeOverListener;
        }
    }

    @Override
    public void setFocusDriveListener(CameraEx.FocusDriveListener focusDriveListener)
    {
        synchronized (locker) {
            this.focusDriveListener = focusDriveListener;
        }
    }

    @Override
    public void setPreviewMagnificationListener(CameraEx.PreviewMagnificationListener previewMagnificationListener)
    {
        synchronized (locker) {
            this.previewMagnificationListener = previewMagnificationListener;
        }
    }

    @Override
    public void setAutoFocusStartListener(CameraEx.AutoFocusStartListener autoFocusStartListener)
    {
        synchronized (locker)
        {
            this.autoFocusStartListener = autoFocusStartListener;
        }
    }

    @Override
    public void setAutoFocusDoneListener(CameraEx.AutoFocusDoneListener autoFocusDoneListener)
    {
        synchronized (locker){
            this.autoFocusDoneListener = autoFocusDoneListener;
        }
    }

    @Override
    public void setCameraEventsListner(CameraInternalEventImpl.CameraEvents eventsListner)
    {
        this.cameraEventsListner = eventsListner;
    }

    @Override
    public void fireOnCameraOpen(boolean isopen)
    {
        if (cameraEventsListner != null)
        {
            Message msg = uiHandler.obtainMessage();
            if (isopen)
                msg.arg1 = 1;
            else
                msg.arg1 = 0;
            msg.what = CAMERAOPEN;
            uiHandler.sendMessage(msg);
        }
    }

    @Override
    public CameraEx.ShutterSpeedInfo getShutterSpeedInfo()
    {
        synchronized (locker) {
            return shutterSpeedInfo;
        }
    }

    private Camera.Parameters getParameters()
    {
        return parameters;
    }

    void setParameters(Camera.Parameters parameters)
    {
        m_camera.getNormalCamera().setParameters(parameters);
    }

    public int getExposureCompensation() {
        return getParameters().getExposureCompensation();
    }

    public void setExposureCompensation(int value) {
        sendMsgToCam(SET_EV,value);
    }

    public int getMaxExposureCompensation() {
        return getParameters().getMaxExposureCompensation();
    }

    public int getMinExposureCompensation() {
        return getParameters().getMinExposureCompensation();
    }

    public float getExposureCompensationStep() {
        return getParameters().getExposureCompensationStep();
    }

    public boolean isLongExposureNoiseReductionSupported()
    {
        return modifier.isSupportedLongExposureNR();
    }

    public void setLongExposureNoiseReduction(boolean enable)
    {
        modifier.setLongExposureNR(enable);
        setParameters(parameters);
    }

    @Override
    public boolean getLongeExposureNR() {
        return modifier.getLongExposureNR();
    }

    public void setFocusMode(String value)
    {
        parameters.setFocusMode(value);
        setParameters(parameters);
    }

    public void setSceneMode(String value)
    {
        parameters.setSceneMode(value);
        setParameters(parameters);
    }

    public String getSceneMode()
    {
        return parameters.getSceneMode();
    }

    public void setDriveMode(String value)
    {
        modifier.setDriveMode(value);
        setParameters(parameters);
    }

    public String getDriveMode()
    {
        return modifier.getDriveMode();
    }

    public void setImageAspectRatio(String value)
    {
        modifier.setImageAspectRatio(value);
        setParameters(parameters);
    }

    public void setBurstDriveSpeed(String value)
    {
        modifier.setBurstDriveSpeed(value);
        setParameters(parameters);
    }

    public String getBurstDriveSpeed()
    {
        return modifier.getBurstDriveSpeed();
    }

    public boolean isAutoShutterSpeedLowLimitSupported()
    {
        return modifier.isSupportedAutoShutterSpeedLowLimit();
    }

    public void setAutoShutterSpeedLowLimit(int value)
    {
        sendMsgToCam(SET_AUTO_SHUTTER_SPEED_LOW_LIMIT,value);
    }

    public int getAutoShutterSpeedLowLimit()
    {
        return modifier.getAutoShutterSpeedLowLimit();
    }

    public void setSelfTimer(int value)
    {
        sendMsgToCam(SET_SELF_TIMER, value);
    }

    public List<Integer> getSupportedISOSensitivities()
    {
        return modifier.getSupportedISOSensitivities();
    }

    public int getISOSensitivity()
    {
        return modifier.getISOSensitivity();
    }

    public void setISOSensitivity(int value)
    {
        sendMsgToCam(SET_ISO,value);
    }

    public void setPreviewMagnification(int factor, Pair position)
    {
        Message msg = cameraHandler.obtainMessage();
        msg.what = SET_PREVIEWMAGNIFICATION;
        msg.arg1 = factor;
        msg.obj = position;
        cameraHandler.sendMessage(msg);
    }

    @Override
    public void stopPreviewMagnification() {
        m_camera.stopPreviewMagnification();
    }

    public List<Integer> getSupportedPreviewMagnification() {
        return modifier.getSupportedPreviewMagnification();
    }

    public void decrementShutterSpeed(){
        sendMsgToCam(DECREASE_SHUTTER);
    }
    public void incrementShutterSpeed()
    {
        sendMsgToCam(INCREASE_SHUTTER);
    }

    public void decrementAperture(){
        sendMsgToCam(DECREASE_APERTURE);
    }

    public void incrementAperture(){
        sendMsgToCam(INCREASE_APERTURE);
    }


    public int getAperture() {
        return modifier.getAperture();
    }

    @Override
    public boolean isImageStabSupported() {
        try {
            modifier.getSupportedAntiHandBlurModes();

            return true;
        }
        catch (NoSuchMethodError ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public String getImageStabilisationMode() {
        return modifier.getAntiHandBlurMode();
    }

    @Override
    public void setImageStabilisation(String enable) {
        modifier.setAntiHandBlurMode(enable);

        setParameters(parameters);
    }

    @Override
    public List<String> getSupportedImageStabModes() {

        return modifier.getSupportedAntiHandBlurModes();
    }

    @Override
    public boolean isLiveSlowShutterSupported() {
        try {
            return modifier.isSupportedSlowShutterLiveviewMode();
        }
        catch (NoSuchMethodError ex)
        {
            ex.printStackTrace();
            return false;
        }
    }

    public void setLiveSlowShutter(String liveSlowShutter)
    {
        modifier.setSlowShutterLiveviewMode(liveSlowShutter);
    }

    @Override
    public String getLiveSlowShutter() {
        return modifier.getSlowShutterLiveviewMode();
    }

    @Override
    public String[] getSupportedLiveSlowShutterModes() {
        return new String[] { modifier.SLOW_SHUTTER_LIVEVIEW_MODE_OFF,modifier.SLOW_SHUTTER_LIVEVIEW_MODE_ON};
    }



    public Pair getShutterSpeed()
    {
        parameters = m_camera.getNormalCamera().getParameters();
        modifier = m_camera.createParametersModifier(parameters);
        return modifier.getShutterSpeed();
    }

    public void adjustShutterSpeed(int val)
    {
        sendMsgToCam(SET_ADJUST_SHUTTER_SPEED,val);
    }

}
