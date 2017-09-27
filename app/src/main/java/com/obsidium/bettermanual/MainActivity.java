package com.obsidium.bettermanual;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.github.killerink.ActivityInterface;
import com.github.killerink.ImageFragment;
import com.github.killerink.KeyEventHandler;
import com.github.killerink.TimeLog;
import com.github.killerink.camera.CameraInstance;
import com.github.killerink.camera.CaptureSession;
import com.github.ma1co.pmcademo.app.BaseActivity;
import com.sony.scalar.hardware.CameraEx;

/**
 * Created by KillerInk on 27.08.2017.
 */

public class MainActivity extends BaseActivity implements ActivityInterface, CameraInstance.CameraEvents, SurfaceHolder.Callback,CameraEx.ShutterListener {

    private final String TAG = MainActivity.class.getSimpleName();
    private Preferences preferences;

    private CameraInstance wrapper;

    public final static int FRAGMENT_CAMERA_UI = 0;
    public final static int FRAGMENT_MIN_SHUTTER = 1;
    public final static int FRAGMENT_PREVIEWMAGNIFICATION = 2;
    public final static int FRAGMENT_IMAGEVIEW = 3;
    public final static int FRAGMENT_WAITFORCAMERARDY = 4;

    private Handler   m_handler;

    private SurfaceHolder m_surfaceHolder;
    SurfaceView surfaceView;

    private boolean isBulbCapture = false;
    private boolean isCaptureInProgress = false;
    private CaptureSession.CaptureDoneEvent eventListner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler))
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());
        m_handler = new Handler();
        setContentView(R.layout.main_activity);

        surfaceView = (SurfaceView)findViewById(R.id.surfaceView);
        //surfaceView.setOnTouchListener(new CameraUiFragment.SurfaceSwipeTouchListener(getContext()));
        m_surfaceHolder = surfaceView.getHolder();
        m_surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        m_surfaceHolder.addCallback(this);
        preferences = new Preferences(getApplicationContext());
    }

    @Override
    protected void onResume() {
        Log.d(TAG,"onResume");
        super.onResume();
        wrapper = new CameraInstance();
        wrapper.setCameraEventsListner(this);



    }

    @Override
    protected void onPause() {
        Log.d(TAG,"onPause");
        super.onPause();
        if (wrapper !=null) {
            saveDefaults();
            wrapper.closeCamera();
            wrapper = null;
        }

    }

    private void saveDefaults()
    {
        // Scene mode
        getPreferences().setSceneMode(getCamera().getSceneMode());
        // Drive mode and burst speed
        getPreferences().setDriveMode(getCamera().getDriveMode());
        getPreferences().setBurstDriveSpeed(getCamera().getBurstDriveSpeed());
    }


    @Override
    public boolean hasTouchScreen() {
        return getDeviceInfo().getModel().compareTo("ILCE-5100") == 0;
    }

    @Override
    public Preferences getPreferences() {
        return preferences;
    }

    @Override
    public KeyEventHandler getDialHandler() {
        return keyEventHandler;
    }

    @Override
    public CameraInstance getCamera() {
        return wrapper;
    }

    @Override
    public Handler getMainHandler() {
        return m_handler;
    }

    @Override
    public void closeApp() {
        // Exiting, make sure the app isn't restarted
        Intent intent = new Intent("com.android.server.DAConnectionManagerService.AppInfoReceive");
        intent.putExtra("package_name", getComponentName().getPackageName());
        intent.putExtra("class_name", getComponentName().getClassName());
        intent.putExtra("pullingback_key", new String[] {});
        intent.putExtra("resume_key", new String[] {});
        sendBroadcast(intent);
        onBackPressed();
    }

    @Override
    public void setColorDepth(boolean highQuality) {
        super.setColorDepth(highQuality);
    }

    @Override
    public void loadFragment(int fragment) {
        switch (fragment)
        {
            case FRAGMENT_MIN_SHUTTER:
                MinShutterFragment msa = MinShutterFragment.getMinShutterActivity(this);
                getDialHandler().setDialEventListner(msa);
                replaceCameraFragment(msa, MinShutterFragment.class.getSimpleName());
                break;
            case FRAGMENT_PREVIEWMAGNIFICATION:
                PreviewMagnificationFragment pmf = PreviewMagnificationFragment.getFragment(this);
                getDialHandler().setDialEventListner(pmf);
                replaceCameraFragment(pmf, PreviewMagnificationFragment.class.getSimpleName());
                break;
            case FRAGMENT_IMAGEVIEW:
                ImageFragment imageFragment = ImageFragment.getFragment(this);
                getDialHandler().setDialEventListner(imageFragment);
                replaceCameraFragment(imageFragment, ImageFragment.class.getSimpleName());
                break;
            case FRAGMENT_WAITFORCAMERARDY:
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.remove(getSupportFragmentManager().findFragmentByTag(ImageFragment.class.getSimpleName()));
                transaction.commit();
                break;
            case FRAGMENT_CAMERA_UI:
            default:
                CameraUiFragment ui = CameraUiFragment.getCameraUiFragment(this);
                getDialHandler().setDialEventListner(ui);
                replaceCameraFragment(ui, CameraUiFragment.class.getSimpleName());
                break;

        }
    }

    @Override
    public void setSurfaceViewOnTouchListner(View.OnTouchListener onTouchListner) {
        if (surfaceView != null)
            surfaceView.setOnTouchListener(onTouchListner);
    }

    @Override
    public String getResString(int id) {
        return getResources().getString(id);
    }

    public void setBulbCapture(boolean bulbCapture)
    {
        this.isBulbCapture = bulbCapture;
    }

    public void setCaptureDoneEventListner(CaptureSession.CaptureDoneEvent eventListner)
    {
        this.eventListner = eventListner;
    }

    public boolean isCaptureInProgress()
    {
        return isCaptureInProgress;
    }

    public boolean isBulbCapture()
    {
        return isBulbCapture;
    }

    public void cancelBulbCapture()
    {
        isBulbCapture = false;
        wrapper.cancleCapture();
    }

    private void replaceCameraFragment(Fragment fragment, String id)
    {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_holder, fragment, id);
        transaction.commit();
    }

    @Override
    public void onCameraOpen(boolean isOpen) {
        Log.d(TAG, "onCameraOpen");
        loadFragment(FRAGMENT_CAMERA_UI);
        loadDefaults();
    }

    private void loadDefaults()
    {
        final TimeLog timeLog = new TimeLog("loadDefaults");
        //Log.d(TAG,"Parameters: " + params.flatten());
        // Focus mode
        getCamera().setFocusMode(CameraEx.ParametersModifier.FOCUS_MODE_MANUAL);
        // Scene mode
        final String sceneMode = getPreferences().getSceneMode();
        getCamera().setSceneMode(sceneMode);
        // Drive mode and burst speed
        getCamera().setDriveMode(getPreferences().getDriveMode());
        getCamera().setBurstDriveSpeed(getPreferences().getBurstDriveSpeed());
        // Minimum shutter speed
        if(getCamera().isAutoShutterSpeedLowLimitSupported()) {
            if (sceneMode.equals(CameraEx.ParametersModifier.SCENE_MODE_MANUAL_EXPOSURE))
                getCamera().setAutoShutterSpeedLowLimit(-1);
            else
                getCamera().setAutoShutterSpeedLowLimit(getPreferences().getMinShutterSpeed());
        }
        // Disable self timer
        getCamera().setSelfTimer(0);
        // Force aspect ratio to 3:2
        getCamera().setImageAspectRatio(CameraEx.ParametersModifier.IMAGE_ASPECT_RATIO_3_2);
        // View visibility

                /*if (getCamera().isLongExposureNoiseReductionSupported())
                    getCamera().setLongExposureNoiseReduction(getP);*/
        timeLog.logTime();

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        wrapper.setSurfaceHolder(surfaceHolder);
        wrapper.startCamera(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    /**
     * Returned from camera when a capture is done
     * STATUS_CANCELED = 1;
     * STATUS_ERROR = 2;
     * STATUS_OK = 0;
     * @param i code
     * @param cameraEx2 did capture Image
     */
    @Override
    public void onShutter(int i, CameraEx cameraEx2) {
        Log.d(TAG, "onShutter:" + logCaptureCode(i)+ " isBulb:" + isBulbCapture);
        Log.d(TAG, "RunMainThread: " + (Thread.currentThread() == Looper.getMainLooper().getThread()));
        if (!isBulbCapture) {
            wrapper.cancleCapture();
            //this.cameraEx.startDirectShutter();
            isCaptureInProgress = false;
            if (eventListner != null)
                eventListner.onCaptureDone();
        }
    }

    private String logCaptureCode(int status)
    {
        switch (status)
        {
            case 1:
                return "Canceled";
            case 2:
                return "Error";
            default:
                return "OK";
        }
    }
}
