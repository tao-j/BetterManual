package com.obsidium.bettermanual.camera;

import com.sony.scalar.hardware.CameraEx;

/**
 * Created by KillerInk on 30.08.2017.
 */

interface CameraEventListnerInterface
{
    CameraEx.AutoPictureReviewControl getAutoPictureReviewControls();
    CameraEx.ShutterSpeedInfo getShutterSpeedInfo();

    void setPreviewAnalizeListener(CameraEx.PreviewAnalizeListener previewAnalizeListener);

    void setFocusDriveListener(CameraEx.FocusDriveListener focusDriveListener);
    void setPreviewMagnificationListener(CameraEx.PreviewMagnificationListener previewMagnificationListener);

    void setCameraEventsListner(BaseCamera.CameraEvents eventsListner);
    void fireOnCameraOpen(boolean isopen);
    void setShutterListener(CameraEx.ShutterListener shutterListener);


}
