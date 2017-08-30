package com.github.killerink.camera;

import com.sony.scalar.hardware.CameraEx;

/**
 * Created by KillerInk on 30.08.2017.
 */

interface CameraEventListnerInterface
{

    int MSG_PREVIEWANALIZELISTNER = 0;
    int MSG_AUTO_ISO_SENSITIVY_LISTNER = 1;
    int MSG_SHUTTERSPEEDCHANGEDLISTNER = 2;
    int MSG_SHUTTERLISTNER = 3;
    int MSG_APERTURECHANGEDLISTNER = 4;
    int MSG_PROGRAM_LINE_RANGE_OVER_LISTNER_EV = 5;
    int MSG_PROGRAM_LINE_RANGE_OVER_LISTNER_AE = 6;
    int MSG_PROGRAM_LINE_RANGE_OVER_LISTNER_METERING = 7;
    int MSG_FOCUS_DRIVE_LISTNER = 7;
    int MSG_PREVIEW_MAGNIFICATION_LISTNER_CHANGED = 9;
    int MSG_PREVIEW_MAGNIFICATION_LISTNER_INFO = 10;
    int MSG_AUTO_FOCUS_START_LISTNER = 11;
    int MSG_AUTO_FOCUS_STOP_LISTNER = 12;
    
    int CAMERAOPEN = 1000;

    CameraEx.AutoPictureReviewControl getAutoPictureReviewControls();
    CameraEx.ShutterSpeedInfo getShutterSpeedInfo();

    void setPreviewAnalizeListener(CameraEx.PreviewAnalizeListener previewAnalizeListener);
    void setAutoISOSensitivityListener(CameraEx.AutoISOSensitivityListener autoISOSensitivityListener);
    void setShutterSpeedChangeListener(CameraEx.ShutterSpeedChangeListener shutterSpeedChangeListener);
    void setShutterListener(CameraEx.ShutterListener shutterListener);
    void setApertureChangeListener(CameraEx.ApertureChangeListener apertureChangeListener);
    void setProgramLineRangeOverListener(CameraEx.ProgramLineRangeOverListener programLineRangeOverListener);
    void setFocusDriveListener(CameraEx.FocusDriveListener focusDriveListener);
    void setPreviewMagnificationListener(CameraEx.PreviewMagnificationListener previewMagnificationListener);
    void setAutoFocusStartListener(CameraEx.AutoFocusStartListener autoFocusStartListener);
    void setAutoFocusDoneListener(CameraEx.AutoFocusDoneListener autoFocusDoneListener);
    void setCameraEventsListner(CameraInternalEventImpl.CameraEvents eventsListner);
    void fireOnCameraOpen(boolean isopen);


}
