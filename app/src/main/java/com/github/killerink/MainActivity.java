package com.github.killerink;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.github.ma1co.pmcademo.app.BaseActivity;
import com.obsidium.bettermanual.CameraUiFragment;
import com.obsidium.bettermanual.CustomExceptionHandler;
import com.obsidium.bettermanual.MinShutterActivity;
import com.obsidium.bettermanual.Preferences;
import com.obsidium.bettermanual.R;

/**
 * Created by troop on 27.08.2017.
 */

public class MainActivity extends BaseActivity implements ActivityInterface, CameraWrapper.CameraEvents {

    private final String TAG = MainActivity.class.getSimpleName();
    private Preferences preferences;
    private HandlerThread mHandlerThread;
    private Handler mbgHandler;

    private CameraWrapper wrapper;

    public final static int FRAGMENT_CAMERA_UI = 0;
    public final static int FRAGMENT_MIN_SHUTTER = 1;

    private Handler   m_handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler))
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());
        m_handler = new Handler(Looper.getMainLooper());
        setContentView(R.layout.fragment_activity);

        preferences = new Preferences(getApplicationContext());
    }

    @Override
    protected void onResume() {
        Log.d(TAG,"onResume");
        super.onResume();
        startBackgroundThread();
        wrapper = new CameraWrapper(this);
        wrapper.setCameraEventsListner(this);
        wrapper.startCamera();


    }

    @Override
    protected void onPause() {
        Log.d(TAG,"onPause");
        super.onPause();
        if (wrapper !=null) {
            wrapper.closeCamera();
            wrapper = null;
        }
        stopBackgroundThread();

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
    public CameraWrapper getCamera() {
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
            case FRAGMENT_CAMERA_UI:
                CameraUiFragment ui = CameraUiFragment.getCameraUiFragment(this);
                replaceCameraFragment(ui, CameraUiFragment.class.getSimpleName());
                break;
            case FRAGMENT_MIN_SHUTTER:
                MinShutterActivity msa = MinShutterActivity.getMinShutterActivity(this);
                replaceCameraFragment(msa, MinShutterActivity.class.getSimpleName());
                break;

        }
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
    }
}
