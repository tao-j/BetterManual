package com.obsidium.bettermanual;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.github.ma1co.pmcademo.app.BaseActivity;
import com.obsidium.bettermanual.camera.CameraInstance;
import com.obsidium.bettermanual.camera.CaptureSession;
import com.sony.scalar.hardware.CameraEx;
import com.sony.scalar.provider.AvindexStore;
import com.sony.scalar.sysutil.didep.Caution;

/**
 * Created by KillerInk on 27.08.2017.
 */

public class MainActivity extends BaseActivity implements ActivityInterface, CameraInstance.CameraEvents, SurfaceHolder.Callback,CameraEx.ShutterListener {

    private final String TAG = MainActivity.class.getSimpleName();
    private Preferences preferences;

    private CameraInstance cameraInstance;

    public final static int FRAGMENT_CAMERA_UI = 0;
    public final static int FRAGMENT_MIN_SHUTTER = 1;
    public final static int FRAGMENT_PREVIEWMAGNIFICATION = 2;
    public final static int FRAGMENT_IMAGEVIEW = 3;
    public final static int FRAGMENT_WAITFORCAMERARDY = 4;

    private Handler   m_handler;

    private SurfaceHolder m_surfaceHolder;
    SurfaceView surfaceView;

    LinearLayout layoutHolder;
    FrameLayout surfaceViewHolder;

    private boolean isBulbCapture = false;
    private boolean isCaptureInProgress = false;
    private CaptureSession.CaptureDoneEvent eventListner;

    private BaseLayout currentLayout;

    private AvIndexHandler avIndexHandler;
    //Caution caution;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler))
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());
        m_handler = new Handler();
        setContentView(R.layout.main_activity);

        surfaceViewHolder = (FrameLayout) findViewById(R.id.surfaceView);
        //surfaceView.setOnTouchListener(new CameraUiFragment.SurfaceSwipeTouchListener(getContext()));
        avIndexHandler = new AvIndexHandler(getContentResolver());

        layoutHolder = (LinearLayout)findViewById(R.id.fragment_holder);
        preferences = new Preferences(getApplicationContext());
    }

    @Override
    protected void onResume() {
        Log.d(TAG,"onResume");
        super.onResume();
        /*caution = new Caution();
        caution.setCallback(new Caution.CautionCallback() {
            @Override
            public void onCallback(int[] ints) {
                Log.d(TAG, "onCautionCallback");
            }
        });*/
        m_handler.post(onResumeRunner);
    }

    private Runnable onResumeRunner =new Runnable() {
        @Override
        public void run() {
            registerReceiver(avIndexHandler, avIndexHandler.AVAILABLE_SIZE_INTENTS);
            registerReceiver(avIndexHandler, avIndexHandler.MEDIA_INTENTS);
            avIndexHandler.onResume(getApplicationContext());
            addSurfaceView();
            cameraInstance = new CameraInstance();
            cameraInstance.setCameraEventsListner(MainActivity.this);
        }
    };

    @Override
    protected void onPause() {
        Log.d(TAG,"onPause");
        removeSurfaceView();
        super.onPause();
        /*caution.setCallback(null);*/
        unregisterReceiver(avIndexHandler);
        avIndexHandler.onPause(getApplicationContext());
        if (cameraInstance !=null) {
            saveDefaults();
            cameraInstance.closeCamera();
            cameraInstance = null;
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
        return cameraInstance;
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
        if (currentLayout != null) {
            currentLayout.Destroy();
            layoutHolder.removeAllViews();
        }
        switch (fragment)
        {
            case FRAGMENT_MIN_SHUTTER:
                MinShutterFragment msa = new MinShutterFragment(getApplicationContext(),this);
                getDialHandler().setDialEventListner(msa);
                currentLayout = msa;
                layoutHolder.addView(msa);
                break;
            case FRAGMENT_PREVIEWMAGNIFICATION:
                PreviewMagnificationFragment pmf = new PreviewMagnificationFragment(getApplicationContext(),this);
                getDialHandler().setDialEventListner(pmf);
                currentLayout = pmf;
                layoutHolder.addView(pmf);
                break;
            case FRAGMENT_IMAGEVIEW:
                getCamera().closeCamera();
                removeSurfaceView();
                ImageFragment imageFragment = new ImageFragment(getApplicationContext(),this);
                getDialHandler().setDialEventListner(imageFragment);
                currentLayout = imageFragment;
                layoutHolder.addView(imageFragment);
                break;
            case FRAGMENT_WAITFORCAMERARDY:
                addSurfaceView();
                break;
            case FRAGMENT_CAMERA_UI:
            default:
                CameraUiFragment cameraUiFragment = new CameraUiFragment(getApplicationContext(),this);
                getDialHandler().setDialEventListner(cameraUiFragment);
                currentLayout = cameraUiFragment;
                layoutHolder.addView(cameraUiFragment);
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

    @Override
    public AvIndexHandler getAvIndexHandler() {
        return avIndexHandler;
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
        cameraInstance.cancleCapture();
    }

    @Override
    public void onCameraOpen(boolean isOpen) {
        Log.d(TAG, "onCameraOpen");
        getCamera().enableHwShutterButton();
        getCamera().setShutterListener(this);
        getCamera().setSurfaceHolder(m_surfaceHolder);
        getCamera().startPreview();


        loadFragment(FRAGMENT_CAMERA_UI);
        loadDefaults();
    }

    private void loadDefaults()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
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
                getCamera().setImageQuality(CameraEx.ParametersModifier.PICTURE_STORAGE_FMT_RAW);
                // View visibility

                /*if (getCamera().isLongExposureNoiseReductionSupported())
                    getCamera().setLongExposureNoiseReduction(getP);*/
                timeLog.logTime();
            }
        }).start();


    }

    private void addSurfaceView() {
        surfaceView = new SurfaceView(getApplicationContext());
        m_surfaceHolder = surfaceView.getHolder();
        m_surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        m_surfaceHolder.addCallback(this);
        surfaceViewHolder.addView(surfaceView);
    }

    private void removeSurfaceView()
    {
        surfaceView = null;
        surfaceViewHolder.removeAllViews();
    }

    @Override
    public void surfaceCreated(final SurfaceHolder surfaceHolder) {
        m_handler.post(new Runnable() {
            @Override
            public void run() {
                cameraInstance.setSurfaceHolder(surfaceHolder);
                cameraInstance.startCamera(MainActivity.this);
            }
        });

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

            cameraInstance.cancleCapture();
            /*Caution.SetTrigger(131078, 1, false);
            avIndexHandler.update();
            Caution.SetMode(2, AvindexStore.getExternalMediaIds());
            String mediaStatus =  Environment.getExternalStorageState();
            Log.d(TAG,"MediaStatus:" + mediaStatus);*/
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
