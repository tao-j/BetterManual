package com.github.killerink;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.github.killerink.camera.CameraInstance;
import com.github.ma1co.pmcademo.app.BaseActivity;
import com.obsidium.bettermanual.CameraUiFragment;
import com.obsidium.bettermanual.CustomExceptionHandler;
import com.obsidium.bettermanual.MinShutterFragment;
import com.obsidium.bettermanual.Preferences;
import com.obsidium.bettermanual.PreviewMagnificationFragment;
import com.obsidium.bettermanual.R;
import com.sony.scalar.hardware.CameraEx;

/**
 * Created by KillerInk on 27.08.2017.
 */

public class MainActivity extends BaseActivity implements ActivityInterface, CameraInstance.CameraEvents {

    private final String TAG = MainActivity.class.getSimpleName();
    private Preferences preferences;
    private HandlerThread mHandlerThread;
    private Handler mbgHandler;

    private CameraInstance wrapper;

    public final static int FRAGMENT_CAMERA_UI = 0;
    public final static int FRAGMENT_MIN_SHUTTER = 1;
    public final static int FRAGMENT_PREVIEWMAGNIFICATION = 2;
    public final static int FRAGMENT_IMAGEVIEW = 3;
    public final static int FRAGMENT_WAITFORCAMERARDY = 4;

    private Handler   m_handler;

    private SurfaceHolder m_surfaceHolder;
    SurfaceView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler))
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());
        m_handler = new Handler(Looper.getMainLooper());
        setContentView(R.layout.main_activity);

        surfaceView = (SurfaceView)findViewById(R.id.surfaceView);
        //surfaceView.setOnTouchListener(new CameraUiFragment.SurfaceSwipeTouchListener(getContext()));
        m_surfaceHolder = surfaceView.getHolder();
        m_surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        preferences = new Preferences(getApplicationContext());
    }

    @Override
    protected void onResume() {
        Log.d(TAG,"onResume");
        super.onResume();
        startBackgroundThread();
        wrapper = new CameraInstance(mHandlerThread);
        m_surfaceHolder.addCallback(wrapper);
        wrapper.setCameraEventsListner(this);
        wrapper.startCamera();


    }

    @Override
    protected void onPause() {
        Log.d(TAG,"onPause");
        super.onPause();
        if (wrapper !=null) {
            saveDefaults();
            m_surfaceHolder.removeCallback(wrapper);
            wrapper.closeCamera();
            wrapper = null;
        }
        stopBackgroundThread();

    }

    private void saveDefaults()
    {
        // Scene mode
        getPreferences().setSceneMode(getCamera().getSceneMode());
        // Drive mode and burst speed
        getPreferences().setDriveMode(getCamera().getDriveMode());
        getPreferences().setBurstDriveSpeed(getCamera().getBurstDriveSpeed());
    }


    public void startBackgroundThread(){
        mHandlerThread = new HandlerThread("HandlerThread");
        mHandlerThread.start();
        mbgHandler = new Handler(mHandlerThread.getLooper());
    }

    private void stopBackgroundThread()
    {
        Log.d(TAG,"stopBackgroundThread");
        if(mHandlerThread == null)
            return;

        mHandlerThread.quit();
        try {
            mHandlerThread.join();
            mHandlerThread = null;
            mHandlerThread = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
    public Handler getBackHandler() {
        return mbgHandler;
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

    private void replaceCameraFragment(Fragment fragment, String id)
    {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_holder, fragment, id);
        transaction.commit();
    }

    @Override
    public void onCameraOpen(boolean isOpen) {
        Log.d(TAG, "onCameraOpen");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadFragment(FRAGMENT_CAMERA_UI);
            }
        });
        loadDefaults();
    }

    private void loadDefaults()
    {
        getBackHandler().post(new Runnable() {
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
                // View visibility

                /*if (getCamera().isLongExposureNoiseReductionSupported())
                    getCamera().setLongExposureNoiseReduction(getP);*/
                timeLog.logTime();
            }
        });

    }
}
