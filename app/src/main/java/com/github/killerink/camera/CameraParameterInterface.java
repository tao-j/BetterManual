package com.github.killerink.camera;

import android.util.Pair;

import java.util.List;

/**
 * Created by KillerInk on 30.08.2017.
 */

interface CameraParameterInterface
{

    int INCREASE_SHUTTER = 1;
    int DECREASE_SHUTTER = 2;
    int DECREASE_APERTURE = 3;
    int INCREASE_APERTURE = 4;
    int SET_ISO = 5;
    int SET_EV =6;
    int SET_AUTO_SHUTTER_SPEED_LOW_LIMIT = 7;
    int SET_SELF_TIMER = 8;
    int SET_PREVIEWMAGNIFICATION = 9;
    int SET_ADJUST_SHUTTER_SPEED = 10;
    int START_PREVIEW = 11;
    int STOP_PREVIEW = 12;
    int START_DISPLAY = 13;
    int STOP_DISPLAY = 14;
    int CAPTURE_IMAGE = 15;
    int CANCEL_CAPTURE = 16;
    
    //ExposureCompensation
    int getExposureCompensation();
    void setExposureCompensation(int value);
    int getMaxExposureCompensation();
    int getMinExposureCompensation();
    float getExposureCompensationStep();

    //long expo noise reduction
    boolean isLongExposureNoiseReductionSupported();
    void setLongExposureNoiseReduction(boolean enable);
    
    void setFocusMode(String value);
    
    void setSceneMode(String value);
    String getSceneMode();
    
    void setDriveMode(String value);
    String getDriveMode();
    
    void setImageAspectRatio(String value);
    
    void setBurstDriveSpeed(String value);
    
    String getBurstDriveSpeed();
    
    boolean isAutoShutterSpeedLowLimitSupported();
    void setAutoShutterSpeedLowLimit(int value);
    int getAutoShutterSpeedLowLimit();

    void setSelfTimer(int value);

    List<Integer> getSupportedISOSensitivities();
    int getISOSensitivity();
    void setISOSensitivity(int value);

    void setPreviewMagnification(int factor, Pair position);

    void decrementShutterSpeed();
    void incrementShutterSpeed();
    Pair getShutterSpeed();
    void adjustShutterSpeed(int val);
    void decrementAperture();
    void incrementAperture();
    int getAperture();

    boolean isImageStabSupported();
    String getImageStabilisationMode();
    void setImageStabilisation(String mode);
    List<String> getSupportedImageStabModes();

    boolean isLiveSlowShutterSupported();
    void setLiveSlowShutter(String liveSlowShutter);
    String getLiveSlowShutter();
    List<String> getSupportedLiveSlowShutterModes();
}
