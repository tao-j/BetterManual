package com.github.killerink.camera;

import android.os.Message;
import android.util.Log;
import android.util.Pair;

import com.sony.scalar.hardware.CameraEx;

/**
 * Created by KillerInk on 28.08.2017.
 */

class CameraInternalEventImpl extends BaseCamera implements  CameraEx.PreviewAnalizeListener, CameraEx.ShutterListener,
        CameraEx.AutoISOSensitivityListener,CameraEx.ShutterSpeedChangeListener,
        CameraEx.ApertureChangeListener, CameraEx.ProgramLineRangeOverListener,
        CameraEx.FocusDriveListener, CameraEx.PreviewMagnificationListener,
        CameraEx.AutoFocusDoneListener, CameraEx.AutoFocusStartListener
{

    private final String TAG = CameraInternalEventImpl.class.getSimpleName();

    CameraInternalEventImpl()
    {
        autoPictureReviewControl = new CameraEx.AutoPictureReviewControl();
    }


    //ImageCaptureCallback
    @Override
    public void onShutter(int i, CameraEx cameraEx) {
        Log.d(TAG,"onShutter");
        //sendMsgToUi(MSG_SHUTTERLISTNER,i);
    }

    @Override
    public void onAnalizedData(CameraEx.AnalizedData analizedData, CameraEx cameraEx) {
        //sendMsgToUi(MSG_PREVIEWANALIZELISTNER, analizedData);
    }

    //ISO AUTO SENSITIVY LISTNER INTERNAL USED
    @Override
    public void onChanged(int i, CameraEx cameraEx) {
        //sendMsgToUi(MSG_AUTO_ISO_SENSITIVY_LISTNER,i);
    }

    @Override
    public void onShutterSpeedChange(CameraEx.ShutterSpeedInfo shutterSpeedInfo, CameraEx cameraEx) {
        synchronized (locker){
            this.shutterSpeedInfo = shutterSpeedInfo;
        }
        //sendMsgToUi(MSG_SHUTTERSPEEDCHANGEDLISTNER, shutterSpeedInfo);
    }

    @Override
    public void onApertureChange(CameraEx.ApertureInfo apertureInfo, CameraEx cameraEx) {
        //sendMsgToUi(MSG_APERTURECHANGEDLISTNER, apertureInfo);
    }

    @Override
    public void onAERange(boolean b, boolean b1, boolean b2, CameraEx cameraEx) {

    }

    @Override
    public void onEVRange(int ev, CameraEx cameraEx) {
        //sendMsgToUi(MSG_AUTO_ISO_SENSITIVY_LISTNER,ev);
    }

    @Override
    public void onMeteringRange(boolean b, CameraEx cameraEx) {

    }

    //FOCUSDRIVE
    @Override
    public void onChanged(CameraEx.FocusPosition focusPosition, CameraEx cameraEx) {
        //sendMsgToUi(MSG_FOCUS_DRIVE_LISTNER,focusPosition);
    }

    //Preview Magnification
    @Override
    public void onChanged(boolean b, int i, int i1, Pair pair, CameraEx cameraEx) {
        /*PreviewMagnificationHelper helper = new PreviewMagnificationHelper(b,i,i1,pair);
        sendMsgToUi(MSG_PREVIEW_MAGNIFICATION_LISTNER_CHANGED, helper);*/
    }

    @Override
    public void onInfoUpdated(boolean b, Pair pair, CameraEx cameraEx) {

    }

    //AutoFocusStop
    @Override
    public void onDone(int i, int[] ints, CameraEx cameraEx) {
        Message msg = uiHandler.obtainMessage();
        msg.what = MSG_AUTO_FOCUS_STOP_LISTNER;
        msg.arg1 = i;
        msg.obj = ints;
        uiHandler.sendMessage(msg);
    }

    //AutoFocusStart
    @Override
    public void onStart(CameraEx cameraEx) {
        //sendMsgToUi(MSG_AUTO_FOCUS_START_LISTNER);
    }


}
