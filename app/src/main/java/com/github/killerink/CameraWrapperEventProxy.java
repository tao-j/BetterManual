package com.github.killerink;

import android.os.Handler;
import android.os.Message;
import android.util.Pair;

import com.sony.scalar.hardware.CameraEx;

/**
 * Created by KillerInk on 28.08.2017.
 */

public class CameraWrapperEventProxy implements CameraEx.PreviewAnalizeListener, CameraEx.ShutterListener,
        CameraEx.AutoISOSensitivityListener,CameraEx.ShutterSpeedChangeListener,
        CameraEx.ApertureChangeListener, CameraEx.ProgramLineRangeOverListener,
        CameraEx.FocusDriveListener, CameraEx.PreviewMagnificationListener
{

    private class PreviewMagnificationHelper
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

    private CameraEx.AutoPictureReviewControl autoPictureReviewControl;
    private CameraEx.PreviewAnalizeListener previewAnalizeListener;

    private CameraEx.AutoISOSensitivityListener autoISOSensitivityListener;

    private CameraEx.ShutterSpeedChangeListener shutterSpeedChangeListener;

    private CameraEx.ShutterListener shutterListener;
    private CameraEx.ApertureChangeListener apertureChangeListener;
    private CameraEx.ProgramLineRangeOverListener programLineRangeOverListener;
    private CameraEx.FocusDriveListener focusDriveListener;
    private CameraEx.PreviewMagnificationListener previewMagnificationListener;


    private final int MSG_PREVIEWANALIZELISTNER = 0;
    private final int MSG_AUTO_ISO_SENSITIVY_LISTNER = 1;
    private final int MSG_SHUTTERSPEEDCHANGEDLISTNER = 2;
    private final int MSG_SHUTTERLISTNER = 3;
    private final int MSG_APERTURECHANGEDLISTNER = 4;
    private final int MSG_PROGRAM_LINE_RANGE_OVER_LISTNER_EV = 5;
    private final int MSG_PROGRAM_LINE_RANGE_OVER_LISTNER_AE = 6;
    private final int MSG_PROGRAM_LINE_RANGE_OVER_LISTNER_METERING = 7;
    private final int MSG_FOCUS_DRIVE_LISTNER = 7;
    private final int MSG_PREVIEW_MAGNIFICATION_LISTNER_CHANGED = 9;
    private final int MSG_PREVIEW_MAGNIFICATION_LISTNER_INFO = 10;


    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what)
            {
                case MSG_PREVIEWANALIZELISTNER:
                    if (previewAnalizeListener != null)
                        previewAnalizeListener.onAnalizedData((CameraEx.AnalizedData)msg.obj,null);
                    break;
                case MSG_AUTO_ISO_SENSITIVY_LISTNER:
                    if (autoISOSensitivityListener != null)
                        autoISOSensitivityListener.onChanged((Integer)msg.obj,null);
                    break;
                case MSG_SHUTTERSPEEDCHANGEDLISTNER:
                    if (shutterSpeedChangeListener != null)
                        shutterSpeedChangeListener.onShutterSpeedChange((CameraEx.ShutterSpeedInfo)msg.obj,null);
                    break;
                case MSG_SHUTTERLISTNER:
                    if (shutterListener != null)
                        shutterListener.onShutter(msg.arg1, null);
                    break;
                case MSG_APERTURECHANGEDLISTNER:
                    if (apertureChangeListener != null)
                        apertureChangeListener.onApertureChange((CameraEx.ApertureInfo)msg.obj,null);
                    break;
                case MSG_PROGRAM_LINE_RANGE_OVER_LISTNER_EV:
                    if (programLineRangeOverListener != null)
                    {
                        programLineRangeOverListener.onEVRange(msg.arg1,null);
                    }
                    break;
                case MSG_FOCUS_DRIVE_LISTNER:
                    if (focusDriveListener != null)
                        focusDriveListener.onChanged((CameraEx.FocusPosition)msg.obj,null);
                    break;
                case MSG_PREVIEW_MAGNIFICATION_LISTNER_CHANGED:
                    if (previewMagnificationListener != null) {
                        PreviewMagnificationHelper helper = (PreviewMagnificationHelper) msg.obj;
                        previewMagnificationListener.onChanged(helper.enable,helper.magFactor,helper.magLevel,helper.coordinates,null);
                    }
                    break;
                default:
                    super.handleMessage(msg);
                    break;

            }
        }
    };

    public CameraWrapperEventProxy()
    {
        autoPictureReviewControl = new CameraEx.AutoPictureReviewControl();
    }

    public CameraEx.AutoPictureReviewControl getAutoPictureReviewControls()
    {
        return autoPictureReviewControl;
    }

    public void setPreviewAnalizeListener(CameraEx.PreviewAnalizeListener previewAnalizeListener)
    {
        this.previewAnalizeListener = previewAnalizeListener;
    }

    public void setAutoISOSensitivityListener(CameraEx.AutoISOSensitivityListener autoISOSensitivityListener)
    {
        this.autoISOSensitivityListener = autoISOSensitivityListener;
    }

    public void setShutterSpeedChangeListener(CameraEx.ShutterSpeedChangeListener shutterSpeedChangeListener)
    {
        this.shutterSpeedChangeListener = shutterSpeedChangeListener;
    }

    public void setShutterListener(CameraEx.ShutterListener shutterListener)
    {
        this.shutterListener = shutterListener;
    }

    public void setApertureChangeListener(CameraEx.ApertureChangeListener apertureChangeListener)
    {
        this.apertureChangeListener = apertureChangeListener;
    }

    public void setProgramLineRangeOverListener(CameraEx.ProgramLineRangeOverListener programLineRangeOverListener)
    {
        this.programLineRangeOverListener = programLineRangeOverListener;
    }

    public void setFocusDriveListener(CameraEx.FocusDriveListener focusDriveListener)
    {
        this.focusDriveListener = focusDriveListener;
    }

    public void setPreviewMagnificationListener(CameraEx.PreviewMagnificationListener previewMagnificationListener)
    {
        this.previewMagnificationListener = previewMagnificationListener;
    }

    //ImageCaptureCallback
    @Override
    public void onShutter(int i, CameraEx cameraEx) {
        Message msg = handler.obtainMessage();
        msg.what = MSG_SHUTTERLISTNER;
        msg.arg1 = i;
        handler.sendMessage(msg);
    }

    @Override
    public void onAnalizedData(CameraEx.AnalizedData analizedData, CameraEx cameraEx) {
        Message msg = handler.obtainMessage();
        msg.what = MSG_PREVIEWANALIZELISTNER;
        msg.obj = analizedData;
        handler.sendMessage(msg);
    }

    //ISO AUTO SENSITIVY LISTNER INTERNAL USED
    @Override
    public void onChanged(int i, CameraEx cameraEx) {
        Message msg = handler.obtainMessage();
        msg.what = MSG_AUTO_ISO_SENSITIVY_LISTNER;
        msg.obj = i;
        handler.sendMessage(msg);
    }

    @Override
    public void onShutterSpeedChange(CameraEx.ShutterSpeedInfo shutterSpeedInfo, CameraEx cameraEx) {
        Message msg = handler.obtainMessage();
        msg.what = MSG_SHUTTERSPEEDCHANGEDLISTNER;
        msg.obj = shutterSpeedInfo;
        handler.sendMessage(msg);
    }

    @Override
    public void onApertureChange(CameraEx.ApertureInfo apertureInfo, CameraEx cameraEx) {
        Message msg = handler.obtainMessage();
        msg.what = MSG_APERTURECHANGEDLISTNER;
        msg.obj = apertureInfo;
        handler.sendMessage(msg);
    }

    @Override
    public void onAERange(boolean b, boolean b1, boolean b2, CameraEx cameraEx) {

    }

    @Override
    public void onEVRange(int ev, CameraEx cameraEx) {
        Message msg = handler.obtainMessage();
        msg.what = MSG_PROGRAM_LINE_RANGE_OVER_LISTNER_EV;
        msg.arg1 = ev;
        handler.sendMessage(msg);
    }

    @Override
    public void onMeteringRange(boolean b, CameraEx cameraEx) {

    }

    //FOCUSDRIVE
    @Override
    public void onChanged(CameraEx.FocusPosition focusPosition, CameraEx cameraEx) {
        Message msg = handler.obtainMessage();
        msg.what= MSG_FOCUS_DRIVE_LISTNER;
        msg.obj = focusPosition;
        handler.sendMessage(msg);

    }

    @Override
    public void onChanged(boolean b, int i, int i1, Pair pair, CameraEx cameraEx) {
        Message msg = handler.obtainMessage();
        PreviewMagnificationHelper helper = new PreviewMagnificationHelper(b,i,i1,pair);
        msg.what = MSG_PREVIEW_MAGNIFICATION_LISTNER_CHANGED;
        msg.obj = helper;
        handler.sendMessage(msg);
    }

    @Override
    public void onInfoUpdated(boolean b, Pair pair, CameraEx cameraEx) {

    }
}
