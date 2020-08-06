package com.obsidium.bettermanual;

import android.app.DAConnectionManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.ma1co.pmcademo.app.BaseActivity;
import com.obsidium.bettermanual.camera.CameraInstance;
import com.obsidium.bettermanual.camera.CaptureSession;
import com.obsidium.bettermanual.controller.BatteryObserverController;
import com.obsidium.bettermanual.controller.ShutterController;
import com.obsidium.bettermanual.layout.BaseLayout;
import com.obsidium.bettermanual.layout.CameraUIFragment;
import com.obsidium.bettermanual.layout.ImageFragment;
import com.obsidium.bettermanual.layout.MinShutterFragment;
import com.obsidium.bettermanual.layout.PreviewMagnificationFragment;
import com.sony.scalar.hardware.CameraEx;


/**
 * Created by KillerInk on 27.08.2017.
 */

public class MainActivity extends BaseActivity implements ActivityInterface, CameraInstance.CameraEvents, SurfaceHolder.Callback, CameraEx.ShutterListener {

    public final static int FRAGMENT_CAMERA_UI = 0;
    public final static int FRAGMENT_MIN_SHUTTER = 1;
    public final static int FRAGMENT_PREVIEW_MAGNIFICATION = 2;
    public final static int FRAGMENT_IMAGE_VIEW = 3;
    public final static int FRAGMENT_WAIT_FOR_CAMERA_RDY = 4;
    private final String TAG = MainActivity.class.getSimpleName();

    private BaseLayout currentLayout;
    private LinearLayout layoutHolder;
    private SurfaceView surfaceView;
    private SurfaceHolder m_surfaceHolder;
    private FrameLayout surfaceViewHolder;

    private AvIndexManager avIndexManager;

    private Handler m_handler;
    private HandlerThread cameraThread;
    private CaptureSession.CaptureDoneEvent eventListner;

    private boolean isBulbCapture = false;
    private boolean isCaptureInProgress = false;

    // The following methods are arranged so that it reflectes the call sequence when the app
    // is first opened.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler))
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());
        m_handler = new Handler();

        ShutterController.GetInstance().bindActivityInterface(this);

        setContentView(R.layout.main_activity);
        surfaceViewHolder = (FrameLayout) findViewById(R.id.surfaceView);
        layoutHolder = (LinearLayout) findViewById(R.id.layout_holder);

        //surfaceView.setOnTouchListener(new CameraUiFragment.SurfaceSwipeTouchListener(getContext()));
        if (AvIndexManager.isSupported()) {
            avIndexManager = new AvIndexManager(getContentResolver(), getApplicationContext());
        }
        Preferences.CREATE(getApplicationContext());
    }


    // Once the first view is loaded, callback will trigger startCamera()

    @Override
    public void surfaceCreated(final SurfaceHolder surfaceHolder) {
        m_handler.post(new Runnable() {
            @Override
            public void run() {
                CameraInstance.GET().setSurfaceHolder(surfaceHolder);
                CameraInstance.GET().startCamera();
            }
        });
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    // In a convoluted way the startCamera will eventually call onCameraOpen

    @Override
    public void onCameraOpen(boolean isOpen) {
        Log.d(TAG, "onCameraOpen");
        CameraInstance.GET().enableHwShutterButton();
        CameraInstance.GET().setShutterListener(this);
        CameraInstance.GET().setSurfaceHolder(m_surfaceHolder);
        CameraInstance.GET().startPreview();

        loadFragment(FRAGMENT_CAMERA_UI);
    }

    @Override
    public void loadFragment(int fragment) {
        if (fragment == FRAGMENT_IMAGE_VIEW && avIndexManager == null) {
            return;
        }
        if (currentLayout != null) {
            currentLayout.Destroy();
            layoutHolder.removeAllViews();
        }
        switch (fragment) {
            case FRAGMENT_MIN_SHUTTER:
                MinShutterFragment msa = new MinShutterFragment(getApplicationContext(), this);
                getDialHandler().setDialEventListener(msa);
                currentLayout = msa;
                layoutHolder.addView(msa);
                break;
            case FRAGMENT_PREVIEW_MAGNIFICATION:
                PreviewMagnificationFragment pmf = new PreviewMagnificationFragment(getApplicationContext(), this);
                getDialHandler().setDialEventListener(pmf);
                currentLayout = pmf;
                layoutHolder.addView(pmf);
                break;
            case FRAGMENT_IMAGE_VIEW:
                CameraInstance.GET().closeCamera();
                removeSurfaceView();
                ImageFragment imageFragment = new ImageFragment(getApplicationContext(), this);
                getDialHandler().setDialEventListener(imageFragment);
                currentLayout = imageFragment;
                layoutHolder.addView(imageFragment);
                break;
            case FRAGMENT_WAIT_FOR_CAMERA_RDY:
                addSurfaceView();
                break;
            case FRAGMENT_CAMERA_UI:
            default:
                CameraUIFragment cameraUIFragment = new CameraUIFragment(getApplicationContext(), this);
                getDialHandler().setDialEventListener(cameraUIFragment);
                currentLayout = cameraUIFragment;
                layoutHolder.addView(cameraUIFragment);
                break;
        }
    }

    private void addSurfaceView() {
        surfaceView = new SurfaceView(getApplicationContext());
        m_surfaceHolder = surfaceView.getHolder();
        m_surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        m_surfaceHolder.addCallback(this);
        surfaceViewHolder.addView(surfaceView);
    }

    private void removeSurfaceView() {
        m_surfaceHolder.removeCallback(this);
        surfaceView = null;
        surfaceViewHolder.removeAllViews();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();

        BatteryObserverController.GetInstance().bindView((TextView) findViewById(R.id.textView_battery));
        if (avIndexManager != null) {
            registerReceiver(avIndexManager, avIndexManager.AVAILABLE_SIZE_INTENTS);
            registerReceiver(avIndexManager, avIndexManager.MEDIA_INTENTS);
            avIndexManager.onResume(getApplicationContext());
        }
        addSurfaceView();

        CameraInstance.GET().setM_cameraEvents(MainActivity.this);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        BatteryObserverController.GetInstance().bindView(null);
        if (avIndexManager != null) {
            unregisterReceiver(avIndexManager);
            avIndexManager.onPause(getApplicationContext());
        }

        CameraInstance.GET().closeCamera();
        removeSurfaceView();
        layoutHolder.removeAllViews();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Preferences.CLEAR();
        avIndexManager = null;
        ShutterController.GetInstance().bindActivityInterface(null);
    }

    @Override
    public void closeApp() {
        // Exiting, make sure the app isn't restarted
        Intent intent = new Intent("com.android.server.DAConnectionManagerService.AppInfoReceive");
        intent.putExtra("package_name", getComponentName().getPackageName());
        intent.putExtra("class_name", getComponentName().getClassName());
        intent.putExtra("pullingback_key", new String[]{});
        intent.putExtra("resume_key", new String[]{});
        sendBroadcast(intent);
        new DAConnectionManager(this).finish();
        finish();
    }

    @Override
    public void setColorDepth(boolean highQuality) {
        super.setColorDepth(highQuality);
    }

    @Override
    public boolean hasTouchScreen() {
        return getDeviceInfo().getModel().compareTo("ILCE-5100") == 0;
    }

    @Override
    public KeyEventHandler getDialHandler() {
        return keyEventHandler;
    }

    @Override
    public Handler getMainHandler() {
        return m_handler;
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

    @Override
    public AvIndexManager getAvIndexManager() {
        return avIndexManager;
    }

    public void setCaptureDoneEventListner(CaptureSession.CaptureDoneEvent eventListner) {
        this.eventListner = eventListner;
    }

    public boolean isCaptureInProgress() {
        return isCaptureInProgress;
    }

    public boolean isBulbCapture() {
        return isBulbCapture;
    }

    public void setBulbCapture(boolean bulbCapture) {
        this.isBulbCapture = bulbCapture;
    }

    public void cancelBulbCapture() {
        isBulbCapture = false;
        CameraInstance.GET().cancelCapture();
    }

    /**
     * Returned from camera when a capture is done
     * STATUS_CANCELED = 1;
     * STATUS_ERROR = 2;
     * STATUS_OK = 0;
     *
     * @param i         code
     * @param cameraEx2 did capture Image
     */
    @Override
    public void onShutter(int i, CameraEx cameraEx2) {
        Log.d(TAG, "onShutter:" + logCaptureCode(i) + " isBulb:" + isBulbCapture);
        Log.d(TAG, "RunMainThread: " + (Thread.currentThread() == Looper.getMainLooper().getThread()));
        if (!isBulbCapture()) {
            cameraEx2.cancelTakePicture();
            //CameraInstance.GET().startPreview();
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

    private String logCaptureCode(int status) {
        switch (status) {
            case 1:
                return "Canceled";
            case 2:
                return "Error";
            default:
                return "OK";
        }
    }
}
