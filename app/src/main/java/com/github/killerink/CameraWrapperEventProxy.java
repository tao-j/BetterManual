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
        CameraEx.FocusDriveListener, CameraEx.PreviewMagnificationListener,
        CameraEx.AutoFocusDoneListener, CameraEx.AutoFocusStartListener
{

    public interface CameraEvents{
        void onCameraOpen(boolean isOpen);
    }

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
    private CameraEx.AutoFocusStartListener autoFocusStartListener;
    private CameraEx.AutoFocusDoneListener autoFocusDoneListener;

    private CameraEvents cameraEventsListner;

    private Object locker = new Object();


    private CameraEx.ShutterSpeedInfo shutterSpeedInfo;


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
    private final int MSG_AUTO_FOCUS_START_LISTNER = 11;
    private final int MSG_AUTO_FOCUS_STOP_LISTNER = 12;

    private final  int CAMERAOPEN = 1000;


    private Handler handler = new Handler()
    {
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
    };

    public CameraWrapperEventProxy()
    {
        autoPictureReviewControl = new CameraEx.AutoPictureReviewControl();
    }

    public CameraEx.AutoPictureReviewControl getAutoPictureReviewControls()
    {
        synchronized (locker) {
            return autoPictureReviewControl;
        }
    }

    public void setPreviewAnalizeListener(CameraEx.PreviewAnalizeListener previewAnalizeListener)
    {
        synchronized (locker) {
            this.previewAnalizeListener = previewAnalizeListener;
        }
    }

    public void setAutoISOSensitivityListener(CameraEx.AutoISOSensitivityListener autoISOSensitivityListener)
    {
        synchronized (locker) {
            this.autoISOSensitivityListener = autoISOSensitivityListener;
        }
    }

    public void setShutterSpeedChangeListener(CameraEx.ShutterSpeedChangeListener shutterSpeedChangeListener)
    {
        synchronized (locker) {
            this.shutterSpeedChangeListener = shutterSpeedChangeListener;
        }
    }

    public void setShutterListener(CameraEx.ShutterListener shutterListener)
    {
        synchronized (locker) {
            this.shutterListener = shutterListener;
        }
    }

    public void setApertureChangeListener(CameraEx.ApertureChangeListener apertureChangeListener)
    {
        synchronized (locker) {
            this.apertureChangeListener = apertureChangeListener;
        }
    }

    public void setProgramLineRangeOverListener(CameraEx.ProgramLineRangeOverListener programLineRangeOverListener)
    {
        synchronized (locker) {
            this.programLineRangeOverListener = programLineRangeOverListener;
        }
    }

    public void setFocusDriveListener(CameraEx.FocusDriveListener focusDriveListener)
    {
        synchronized (locker) {
            this.focusDriveListener = focusDriveListener;
        }
    }

    public void setPreviewMagnificationListener(CameraEx.PreviewMagnificationListener previewMagnificationListener)
    {
        synchronized (locker) {
            this.previewMagnificationListener = previewMagnificationListener;
        }
    }

    public void setAutoFocusStartListener(CameraEx.AutoFocusStartListener autoFocusStartListener)
    {
        synchronized (locker)
        {
            this.autoFocusStartListener = autoFocusStartListener;
        }
    }

    public void setAutoFocusDoneListener(CameraEx.AutoFocusDoneListener autoFocusDoneListener)
    {
        synchronized (locker){
            this.autoFocusDoneListener = autoFocusDoneListener;
        }
    }

    public void setCameraEventsListner(CameraEvents eventsListner)
    {
        this.cameraEventsListner = eventsListner;
    }

    public void fireOnCameraOpen(boolean isopen)
    {
        if (cameraEventsListner != null)
        {
            Message msg = handler.obtainMessage();
            if (isopen)
                msg.arg1 = 1;
            else
                msg.arg1 = 0;
            msg.what = CAMERAOPEN;
            handler.sendMessage(msg);
        }
    }

    public CameraEx.ShutterSpeedInfo getShutterSpeedInfo()
    {
        synchronized (locker) {
            return shutterSpeedInfo;
        }
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
        msg.arg1 = i;
        handler.sendMessage(msg);
    }

    @Override
    public void onShutterSpeedChange(CameraEx.ShutterSpeedInfo shutterSpeedInfo, CameraEx cameraEx) {
        synchronized (locker){
            this.shutterSpeedInfo = shutterSpeedInfo;
        }
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

    //Preview Magnification
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

    @Override
    public void onDone(int i, int[] ints, CameraEx cameraEx) {
        Message msg = handler.obtainMessage();
        msg.what = MSG_AUTO_FOCUS_STOP_LISTNER;
        msg.arg1 = i;
        msg.obj = ints;
        handler.sendMessage(msg);
    }

    @Override
    public void onStart(CameraEx cameraEx) {
        Message msg = handler.obtainMessage();
        msg.what = MSG_AUTO_FOCUS_START_LISTNER;
        handler.sendMessage(msg);
    }


}
