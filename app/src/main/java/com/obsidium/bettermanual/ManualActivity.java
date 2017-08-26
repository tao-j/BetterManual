package com.obsidium.bettermanual;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.ma1co.pmcademo.app.BaseActivity;
import com.github.ma1co.pmcademo.app.DialPadKeysEvents;
import com.obsidium.bettermanual.capture.CaptureModeBracket;
import com.obsidium.bettermanual.capture.CaptureModeTimelapse;
import com.obsidium.bettermanual.views.ApertureView;
import com.obsidium.bettermanual.views.BaseImageView;
import com.obsidium.bettermanual.views.BaseTextView;
import com.obsidium.bettermanual.views.DialValueSet;
import com.obsidium.bettermanual.views.DriveMode;
import com.obsidium.bettermanual.views.EvView;
import com.obsidium.bettermanual.views.ExposureModeView;
import com.obsidium.bettermanual.views.FocusScaleView;
import com.obsidium.bettermanual.views.GridView;
import com.obsidium.bettermanual.views.HistogramView;
import com.obsidium.bettermanual.views.IsoView;
import com.obsidium.bettermanual.views.PreviewNavView;
import com.obsidium.bettermanual.views.ShutterView;
import com.sony.scalar.hardware.CameraEx;
import com.sony.scalar.sysutil.ScalarInput;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



public class ManualActivity extends BaseActivity implements SurfaceHolder.Callback, View.OnClickListener, CameraEx.ShutterListener, ActivityInterface
{

    private static final boolean LOGGING_ENABLED = false;
    private static final int MESSAGE_TIMEOUT = 1000;
    private final  String TAG  = ManualActivity.class.getSimpleName();

    private SurfaceHolder   m_surfaceHolder;
    protected CameraEx        m_camera;
    private CameraEx.AutoPictureReviewControl m_autoReviewControl;
    private int             m_pictureReviewTime;

    protected Preferences     m_prefs;

    private ShutterView        m_tvShutter;
    private ApertureView aperture;
    private IsoView     iso;
    private EvView evCompensation;
    private TextView        m_tvExposure;
    private TextView        m_tvLog;
    private TextView        m_tvMagnification;
    private TextView        m_tvMsg;
    private HistogramView m_vHist;
    private DriveMode driveMode;
    protected ExposureModeView exposureMode;
    private ImageView       m_ivTimelapse;
    private ImageView       m_ivBracket;
    private GridView m_vGrid;
    protected TextView        m_tvHint;
    private FocusScaleView m_focusScaleView;
    private View            m_lFocusScale;

    private LinearLayout bottomHolder;
    LinearLayout leftHolder;

    private List<View> dialViews;
    private int lastDialView;

    // Timelapse

    private CaptureModeTimelapse timelapse;
    private CaptureModeBracket bracket;

    private final Runnable m_hideFocusScaleRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            m_lFocusScale.setVisibility(View.GONE);
        }
    };

    // Exposure compensation

    // Preview magnification
    private List<Integer>   m_supportedPreviewMagnifications;
    private boolean         m_zoomLeverPressed;
    private int             m_curPreviewMagnification;
    private float           m_curPreviewMagnificationFactor;
    private Pair<Integer, Integer>  m_curPreviewMagnificationPos = new Pair<Integer, Integer>(0, 0);
    private int             m_curPreviewMagnificationMaxPos;
    private PreviewNavView m_previewNavView;
    private HandlerThread mHandlerThread;
    private Handler mbgHandler;

/*    public enum DialMode { shutter, aperture, iso, exposure, mode, drive,
        timelapse, bracket,
        timelapseSetInterval, timelapseSetPicCount,
        bracketSetStep, bracketSetPicCount
    }

    public DialMode        m_dialMode;*/

    protected final Handler   m_handler = new Handler();

    private final Runnable  m_hideMessageRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            m_tvMsg.setVisibility(View.GONE);
        }
    };

    private boolean         m_takingPicture;
    private boolean         m_shutterKeyDown;

    private boolean         m_haveTouchscreen;

    private static final int VIEW_FLAG_GRID         = 0x01;
    private static final int VIEW_FLAG_HISTOGRAM    = 0x02;
    private static final int VIEW_FLAG_EXPOSURE     = 0x04;
    private static final int VIEW_FLAG_MASK         = 0x07; // all flags combined
    protected int             m_viewFlags;


    private final int DIAL_EXPOSURE=0;
    private final int DIAL_DRIVE =1;
    private final int DIAL_TIMELAPSE =2;
    private final int DIAL_BRACKET =3;
    private final int DIAL_SHUTTER =4;
    private final int DIAL_APERTURE =5;
    private final int DIAL_ISO =6;
    private final int DIAL_EV =7;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_manual);

        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler))
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());

        dialViews = new ArrayList<View>();

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceView.setOnTouchListener(new SurfaceSwipeTouchListener(this));
        m_surfaceHolder = surfaceView.getHolder();
        m_surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        exposureMode = (ExposureModeView) findViewById(R.id.ivMode);
        exposureMode.setOnClickListener(this);
        exposureMode.setActivity(this);
        dialViews.add(exposureMode);

        driveMode = (DriveMode) findViewById(R.id.ivDriveMode);
        driveMode.setOnClickListener(this);
        driveMode.setActivity(this);
        dialViews.add(driveMode);

        m_ivTimelapse = (ImageView)findViewById(R.id.ivTimelapse);
        //noinspection ResourceType
        m_ivTimelapse.setImageResource(SonyDrawables.p_16_dd_parts_43_shoot_icon_setting_drivemode_invalid);
        m_ivTimelapse.setOnClickListener(this);
        dialViews.add(m_ivTimelapse);

        m_ivBracket = (ImageView)findViewById(R.id.ivBracket);
        //noinspection ResourceType
        m_ivBracket.setImageResource(SonyDrawables.p_16_dd_parts_contshot);
        m_ivBracket.setOnClickListener(this);
        dialViews.add(m_ivBracket);

        m_tvShutter = (ShutterView)findViewById(R.id.tvShutter);
        m_tvShutter.setOnTouchListener(m_tvShutter.getSwipeTouchListner());
        m_tvShutter.setActivityInterface(this);
        dialViews.add(m_tvShutter);

        aperture = (ApertureView) findViewById(R.id.tvAperture);
        aperture.setOnTouchListener(aperture.getSwipeTouchListner());
        aperture.setActivityInterface(this);
        dialViews.add(aperture);

        iso = (IsoView) findViewById(R.id.tvISO);
        iso.setOnTouchListener(iso.getSwipeTouchListner());
        iso.setActivityInterface(this);
        dialViews.add(iso);

        evCompensation = (EvView) findViewById(R.id.tvExposureCompensation);
        evCompensation.setOnTouchListener(evCompensation.getSwipeTouchListner());
        evCompensation.setActivityInterface(this);
        dialViews.add(evCompensation);

        bottomHolder = (LinearLayout)findViewById(R.id.bottom_holder);
        leftHolder = (LinearLayout) findViewById(R.id.left_holder);

        m_tvExposure = (TextView)findViewById(R.id.tvExposure);
        //noinspection ResourceType
        m_tvExposure.setCompoundDrawablesWithIntrinsicBounds(SonyDrawables.p_meteredmanualicon, 0, 0, 0);

        m_tvLog = (TextView)findViewById(R.id.tvLog);
        m_tvLog.setVisibility(LOGGING_ENABLED ? View.VISIBLE : View.GONE);

        m_vHist = (HistogramView)findViewById(R.id.vHist);

        m_tvMagnification = (TextView)findViewById(R.id.tvMagnification);

        m_previewNavView = (PreviewNavView)findViewById(R.id.vPreviewNav);
        m_previewNavView.setVisibility(View.GONE);

        m_tvMsg = (TextView)findViewById(R.id.tvMsg);



        m_vGrid = (GridView)findViewById(R.id.vGrid);

        m_tvHint = (TextView)findViewById(R.id.tvHint);
        m_tvHint.setVisibility(View.GONE);

        m_focusScaleView = (FocusScaleView)findViewById(R.id.vFocusScale);
        m_lFocusScale = findViewById(R.id.lFocusScale);
        m_lFocusScale.setVisibility(View.GONE);

        //noinspection ResourceType
        ((ImageView)findViewById(R.id.ivFocusRight)).setImageResource(SonyDrawables.p_16_dd_parts_rec_focuscontrol_far);
        //noinspection ResourceType
        ((ImageView)findViewById(R.id.ivFocusLeft)).setImageResource(SonyDrawables.p_16_dd_parts_rec_focuscontrol_near);

        setDialMode(0);

        m_prefs = new Preferences(this);

        m_haveTouchscreen = getDeviceInfo().getModel().compareTo("ILCE-5100") == 0;
        timelapse = new CaptureModeTimelapse(this);
        bracket = new CaptureModeBracket(this);
    }

    private class SurfaceSwipeTouchListener extends OnSwipeTouchListener
    {
        public SurfaceSwipeTouchListener(Context context)
        {
            super(context);
        }

        @Override
        public boolean onScrolled(float distanceX, float distanceY)
        {
            if (m_curPreviewMagnification != 0)
            {
                m_curPreviewMagnificationPos = new Pair<Integer, Integer>(Math.max(Math.min(m_curPreviewMagnificationMaxPos, m_curPreviewMagnificationPos.first + (int)distanceX), -m_curPreviewMagnificationMaxPos),
                        Math.max(Math.min(m_curPreviewMagnificationMaxPos, m_curPreviewMagnificationPos.second + (int)distanceY), -m_curPreviewMagnificationMaxPos));
                m_camera.setPreviewMagnification(m_curPreviewMagnification, m_curPreviewMagnificationPos);
                return true;
            }
            return false;
        }
    }

    public void showMessageDelayed(String msg)
    {
        showMessage(msg);
        m_handler.removeCallbacks(m_hideMessageRunnable);
        m_handler.postDelayed(m_hideMessageRunnable, MESSAGE_TIMEOUT);
    }

    public void showMessage(String msg)
    {
        m_tvMsg.setText(msg);
        m_tvMsg.setVisibility(View.VISIBLE);
    }

    public void hideMessage()
    {
        m_tvMsg.setVisibility(View.GONE);
    }

    @Override
    public void showHintMessage(String msg) {
        m_tvHint.setText(msg);
        m_tvHint.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideHintMessage() {
        m_tvHint.setVisibility(View.GONE);
    }

    @Override
    public int getActiveViewsFlag() {
        return m_viewFlags;
    }

    @Override
    public void setActiveViewFlag(int viewsToShow) {
        m_viewFlags = viewsToShow;
    }

    public Preferences getPreferences()
    {
        return m_prefs;
    }


    public CameraEx getCamera()
    {
        return m_camera;
    }

    @Override
    public ExposureModeView getExposureMode() {
        return exposureMode;
    }

    @Override
    public Handler getMainHandler() {
        return m_handler;
    }

    @Override
    public Handler getBackHandler() {
        return mbgHandler;
    }
    private void log(final String str)
    {
        if (LOGGING_ENABLED)
            m_tvLog.append(str);
    }

    @Override
    public void startActivity(Class<?> activity) {
        startActivity(new Intent(getApplicationContext(), activity));
    }

    @Override
    public void takePicture() {
        m_camera.burstableTakePicture();
    }

    @Override
    public DialHandler getDialHandler() {
        return dialHandler;
    }

    /**
     * Returned from camera when a capture is done
     * STATUS_CANCELED = 1;
     * STATUS_ERROR = 2;
     * STATUS_OK = 0;
     * @param i code
     * @param cameraEx did capture Image
     */
    @Override
    public void onShutter(int i, CameraEx cameraEx)
    {
        if (i != 0)
        {
            m_takingPicture = false;
        }
        if (!bulbcapture) {

            m_camera.cancelTakePicture();
            if (timelapse.isActive())
                timelapse.onShutter(i);
            else if (bracket.isActive())
                bracket.onShutter(i);
        }
    }


    public void updateViewVisibility()
    {
        m_vHist.setVisibility((m_viewFlags & VIEW_FLAG_HISTOGRAM) != 0 ? View.VISIBLE : View.GONE);
        m_vGrid.setVisibility((m_viewFlags & VIEW_FLAG_GRID) != 0 ? View.VISIBLE : View.GONE);
    }

    private void cycleVisibleViews()
    {
        if (++m_viewFlags > VIEW_FLAG_MASK)
            m_viewFlags = 0;
        updateViewVisibility();
    }

    private void dumpList(List list, String name)
    {
        log(name);
        log(": ");
        if (list != null)
        {
            for (Object o : list)
            {
                log(o.toString());
                log(" ");
            }
        }
        else
            log("null");
        log("\n");
    }

    private void togglePreviewMagnificationViews(boolean magnificationActive)
    {
        m_previewNavView.setVisibility(magnificationActive ? View.VISIBLE : View.GONE);
        m_tvMagnification.setVisibility(magnificationActive ? View.VISIBLE : View.GONE);
        m_vHist.setVisibility(magnificationActive ? View.GONE : View.VISIBLE);
        setLeftViewVisibility(!magnificationActive);
    }



    private void saveDefaults()
    {
        final Camera.Parameters params = m_camera.getNormalCamera().getParameters();
        final CameraEx.ParametersModifier paramsModifier = m_camera.createParametersModifier(params);
        // Scene mode
        m_prefs.setSceneMode(params.getSceneMode());
        // Drive mode and burst speed
        m_prefs.setDriveMode(paramsModifier.getDriveMode());
        m_prefs.setBurstDriveSpeed(paramsModifier.getBurstDriveSpeed());
        // View visibility
        m_prefs.setViewFlags(m_viewFlags);

        // TODO: Dial mode
    }

    private void disableLENR()
    {
        // Disable long exposure noise reduction
        final Camera.Parameters params = m_camera.createEmptyParameters();
        final CameraEx.ParametersModifier paramsModifier = m_camera.createParametersModifier(m_camera.getNormalCamera().getParameters());
        final CameraEx.ParametersModifier modifier = m_camera.createParametersModifier(params);
        if (paramsModifier.isSupportedLongExposureNR())
            modifier.setLongExposureNR(false);
        m_camera.getNormalCamera().setParameters(params);
    }

    private void loadDefaults()
    {
        final Camera.Parameters params = m_camera.createEmptyParameters();
        final CameraEx.ParametersModifier modifier = m_camera.createParametersModifier(params);
        //Log.d(TAG,"Parameters: " + params.flatten());
        // Focus mode
        params.setFocusMode(CameraEx.ParametersModifier.FOCUS_MODE_MANUAL);
        // Scene mode
        final String sceneMode = m_prefs.getSceneMode();
        params.setSceneMode(sceneMode);
        // Drive mode and burst speed
        modifier.setDriveMode(m_prefs.getDriveMode());
        modifier.setBurstDriveSpeed(m_prefs.getBurstDriveSpeed());
        // Minimum shutter speed
        if (sceneMode.equals(CameraEx.ParametersModifier.SCENE_MODE_MANUAL_EXPOSURE))
            modifier.setAutoShutterSpeedLowLimit(-1);
        else
            modifier.setAutoShutterSpeedLowLimit(m_prefs.getMinShutterSpeed());
        // Disable self timer
        modifier.setSelfTimer(0);
        // Force aspect ratio to 3:2
        modifier.setImageAspectRatio(CameraEx.ParametersModifier.IMAGE_ASPECT_RATIO_3_2);
        // Apply
        m_camera.getNormalCamera().setParameters(params);
        // View visibility
        m_viewFlags = m_prefs.getViewFlags(VIEW_FLAG_GRID | VIEW_FLAG_HISTOGRAM);
        // TODO: Dial mode?
        setDialMode(0);

        disableLENR();
    }



    @Override
    protected void onResume()
    {
        super.onResume();
        dialHandler.setDialEventListner(this);
        startBackgroundThread();
        m_camera = CameraEx.open(0, null);
        m_surfaceHolder.addCallback(this);
        m_camera.startDirectShutter();
        m_autoReviewControl = new CameraEx.AutoPictureReviewControl();
        m_camera.setAutoPictureReviewControl(m_autoReviewControl);
        // Disable picture review
        m_pictureReviewTime = m_autoReviewControl.getPictureReviewTime();
        m_autoReviewControl.setPictureReviewTime(0);

        m_vGrid.setVideoRect(getDisplayManager().getDisplayedVideoRect());

        final Camera.Parameters params = m_camera.getNormalCamera().getParameters();
        final CameraEx.ParametersModifier paramsModifier = m_camera.createParametersModifier(params);

        // Exposure compensation
        evCompensation.init(params.getMaxExposureCompensation(),params.getMinExposureCompensation(),params.getExposureCompensationStep(),params.getExposureCompensation());

        // Preview/Histogram
        m_camera.setPreviewAnalizeListener(new CameraEx.PreviewAnalizeListener()
        {
            @Override
            public void onAnalizedData(CameraEx.AnalizedData analizedData, CameraEx cameraEx)
            {
                if (analizedData != null && analizedData.hist != null && analizedData.hist.Y != null && m_vHist.getVisibility() == View.VISIBLE)
                    m_vHist.setHistogram(analizedData.hist.Y);
            }
        });


        // ISO
        m_camera.setAutoISOSensitivityListener(iso);

        // Shutter
        m_camera.setShutterSpeedChangeListener(bracket);
        //returns when a capture is done, seems to replace the default android camera1 api CaptureCallback that get called with Camera.takePicture(shutter,raw, jpeg)
        //also it seems Camera.takePicture is nonfunctional/crash on a6000
        m_camera.setShutterListener(this);

        //m_camera.setJpegListener(); maybe is used to get jpeg/raw data returned

        // Aperture
        m_camera.setApertureChangeListener(aperture);

        // Exposure metering
        m_camera.setProgramLineRangeOverListener(new CameraEx.ProgramLineRangeOverListener()
        {
            @Override
            public void onAERange(boolean b, boolean b1, boolean b2, CameraEx cameraEx)
            {
                //log(String.format("onARRange b %b b1 %b b2 %b\n", Boolean.valueOf(b), Boolean.valueOf(b1), Boolean.valueOf(b2)));
            }

            @Override
            public void onEVRange(int ev, CameraEx cameraEx)
            {
                final String text;
                if (ev == 0)
                    text = "\u00B10.0";
                else if (ev > 0)
                    text = String.format("+%.1f", (float)ev / 3.0f);
                else
                    text = String.format("%.1f", (float)ev / 3.0f);
                m_tvExposure.setText(text);
                //log(String.format("onEVRange i %d %f\n", ev, (float)ev / 3.0f));
            }

            @Override
            public void onMeteringRange(boolean b, CameraEx cameraEx)
            {
                //log(String.format("onMeteringRange b %b\n", Boolean.valueOf(b)));
            }
        });

        iso.init((List<Integer>)paramsModifier.getSupportedISOSensitivities(), paramsModifier.getISOSensitivity());

        aperture.setText(String.format("f%.1f", (float)paramsModifier.getAperture() / 100.0f));

        Pair<Integer, Integer> sp = paramsModifier.getShutterSpeed();
        m_tvShutter.updateShutterSpeed(sp.first, sp.second);

        m_supportedPreviewMagnifications = (List<Integer>)paramsModifier.getSupportedPreviewMagnification();
        m_camera.setPreviewMagnificationListener(new CameraEx.PreviewMagnificationListener()
        {
            @Override
            public void onChanged(boolean enabled, int magFactor, int magLevel, Pair coords, CameraEx cameraEx)
            {
                // magnification / 100 = x.y
                // magLevel = value passed to setPreviewMagnification
                /*
                m_tvLog.setText("onChanged enabled:" + String.valueOf(enabled) + " magFactor:" + String.valueOf(magFactor) + " magLevel:" +
                    String.valueOf(magLevel) + " x:" + coords.first + " y:" + coords.second + "\n");
                */
                if (enabled)
                {
                    //log("m_curPreviewMagnificationMaxPos: " + String.valueOf(m_curPreviewMagnificationMaxPos) + "\n");
                    m_curPreviewMagnification = magLevel;
                    m_curPreviewMagnificationFactor = ((float)magFactor / 100.0f);
                    m_curPreviewMagnificationMaxPos = 1000 - (int)(1000.0f / m_curPreviewMagnificationFactor);
                    m_tvMagnification.setText(String.format("\uE012 %.2fx", (float)magFactor / 100.0f));
                    m_previewNavView.update(coords, m_curPreviewMagnificationFactor);
                }
                else
                {
                    m_previewNavView.update(null, 0);
                    m_curPreviewMagnification = 0;
                    m_curPreviewMagnificationMaxPos = 0;
                    m_curPreviewMagnificationFactor = 0;
                }
                togglePreviewMagnificationViews(enabled);
            }

            @Override
            public void onInfoUpdated(boolean b, Pair coords, CameraEx cameraEx)
            {
                // Useless?
                /*
                log("onInfoUpdated b:" + String.valueOf(b) +
                               " x:" + coords.first + " y:" + coords.second + "\n");
                */
            }
        });

        m_camera.setFocusDriveListener(new CameraEx.FocusDriveListener()
        {
            @Override
            public void onChanged(CameraEx.FocusPosition focusPosition, CameraEx cameraEx)
            {
                if (m_curPreviewMagnification == 0)
                {
                    m_lFocusScale.setVisibility(View.VISIBLE);
                    m_focusScaleView.setMaxPosition(focusPosition.maxPosition);
                    m_focusScaleView.setCurPosition(focusPosition.currentPosition);
                    m_handler.removeCallbacks(m_hideFocusScaleRunnable);
                    m_handler.postDelayed(m_hideFocusScaleRunnable, 2000);
                }
            }
        });

        loadDefaults();
        driveMode.updateImage();
        exposureMode.updateImage();
        updateViewVisibility();
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        saveDefaults();

        m_surfaceHolder.removeCallback(this);
        m_autoReviewControl.setPictureReviewTime(m_pictureReviewTime);
        m_camera.setAutoPictureReviewControl(null);
        m_camera.getNormalCamera().stopPreview();
        m_camera.release();
        m_camera = null;
        stopBackgroundThread();
    }

    // OnClickListener
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.ivDriveMode:
                driveMode.toggle();
                break;
            case R.id.ivMode:
                exposureMode.toggle();
                break;
            case R.id.ivTimelapse:
                timelapse.prepare();
                break;
            case R.id.ivBracket:
                bracket.prepare();
                break;
        }
    }



    @Override
    public IsoView getIso() {
        return iso;
    }

    @Override
    public ShutterView getShutter() {
        return m_tvShutter;
    }

    @Override
    public DriveMode getDriveMode() {
        return driveMode;
    }

    @Override
    public ApertureView getAperture() {
        return aperture;
    }


    public void setLeftViewVisibility(boolean visible)
    {
        final int visibility = visible ? View.VISIBLE : View.GONE;
        leftHolder.setVisibility(visibility);
    }

    @Override
    public boolean onUpperDialChanged(int value)
    {
        DialValueSet view = (DialValueSet) dialViews.get(lastDialView);
        view.setIn_DecrementValue(value);
        return true;
    }

    @Override
    public boolean onLowerDialChanged(int value) {

        setDialMode(value);

        return true;
    }

    public void setDialMode(int mode)
    {
        View lastView = dialViews.get(lastDialView);
        if (lastView instanceof BaseTextView)
        {
            ((BaseTextView)lastView).setTextColor(Color.WHITE);
        }
        else if (lastView instanceof BaseImageView)
        {
            ((BaseImageView)lastView).setColorFilter(null);
        }
        else if (lastView instanceof ImageView)
            ((ImageView)lastView).setColorFilter(null);
        lastDialView = lastDialView + mode;
        if (lastDialView >= dialViews.size())
            lastDialView = 0;
        else if(lastDialView < 0)
            lastDialView = dialViews.size()-1;

        lastView = dialViews.get(lastDialView);
        if (lastView instanceof BaseTextView)
        {
            ((BaseTextView)lastView).setTextColor(Color.GREEN);
        }
        else if (lastView instanceof BaseImageView)
        {
            ((BaseImageView)lastView).setColorFilter(Color.GREEN);
        }
        else if (lastView instanceof ImageView)
            ((ImageView)lastView).setColorFilter(Color.GREEN);
    }

    /*public void setDialMode(DialMode newMode)
    {
        m_dialMode = newMode;
        m_tvShutter.setTextColor(newMode == DialMode.shutter ? Color.GREEN : Color.WHITE);
        aperture.setTextColor(newMode == DialMode.aperture ? Color.GREEN : Color.WHITE);
        iso.setTextColor(newMode == DialMode.iso ? Color.GREEN : Color.WHITE);
        evCompensation.setTextColor(newMode == DialMode.exposure ? Color.GREEN : Color.WHITE);
        if (newMode == DialMode.mode)
            exposureMode.setColorFilter(Color.GREEN);
        else
            exposureMode.setColorFilter(null);
        if (newMode == DialMode.drive)
            driveMode.setColorFilter(Color.GREEN);
        else
            driveMode.setColorFilter(null);
        if (newMode == DialMode.timelapse)
            m_ivTimelapse.setColorFilter(Color.GREEN);
        else
            m_ivTimelapse.setColorFilter(null);
        if (newMode == DialMode.bracket)
            m_ivBracket.setColorFilter(Color.GREEN);
        else
            m_ivBracket.setColorFilter(null);
    }*/

    private void movePreviewVertical(int delta)
    {
        int newY = m_curPreviewMagnificationPos.second + delta;
        if (newY > m_curPreviewMagnificationMaxPos)
            newY = m_curPreviewMagnificationMaxPos;
        else if (newY < -m_curPreviewMagnificationMaxPos)
            newY = -m_curPreviewMagnificationMaxPos;
        m_curPreviewMagnificationPos = new Pair<Integer, Integer>(m_curPreviewMagnificationPos.first, newY);
        m_camera.setPreviewMagnification(m_curPreviewMagnification, m_curPreviewMagnificationPos);
    }

    private void movePreviewHorizontal(int delta)
    {
        int newX = m_curPreviewMagnificationPos.first + delta;
        if (newX > m_curPreviewMagnificationMaxPos)
            newX = m_curPreviewMagnificationMaxPos;
        else if (newX < -m_curPreviewMagnificationMaxPos)
            newX = -m_curPreviewMagnificationMaxPos;
        m_curPreviewMagnificationPos = new Pair<Integer, Integer>(newX, m_curPreviewMagnificationPos.second);
        m_camera.setPreviewMagnification(m_curPreviewMagnification, m_curPreviewMagnificationPos);
    }

    @Override
    public boolean onEnterKeyUp()
    {
        return true;
    }

    @Override
    public boolean onEnterKeyDown()
    {
        View view = dialViews.get(lastDialView);
        Log.d(TAG,"onEnterKeyDown");
        if (timelapse.isActive())
        {
            timelapse.abort();
            return false;
        }
        else if (bracket.isActive())
        {
            bracket.abort();
            return false;
        }
        else if (m_curPreviewMagnification != 0)
        {
            m_curPreviewMagnificationPos = new Pair<Integer, Integer>(0, 0);
            m_camera.setPreviewMagnification(m_curPreviewMagnification, m_curPreviewMagnificationPos);
            return true;
        }
        else if (view instanceof ShutterView || bulbcapture)
        {
            if (m_tvShutter.getText().equals("BULB")) {
                if (!bulbcapture) {
                    startBulbCapture();
                    return false;

                } else if (bulbcapture) {

                    stopBulbCapture();
                    return false;
                }
            }
            else
                getShutter().onClick();
            return false;
        }
        else if (view == m_ivTimelapse)
        {
            dialHandler.setDialEventListner(timelapse);
            timelapse.onEnterKeyDown();

            return false;
        }
        else if (view == m_ivBracket)
        {
            dialHandler.setDialEventListner(bracket);
            bracket.onEnterKeyDown();
            //setDialMode(DialMode.bracketSetPicCount);

            return false;
        }
        else if (view == exposureMode)
        {
            exposureMode.toggle();
            return false;
        }
        else if (view == driveMode)
        {
            driveMode.toggle();
            return false;
        }
        else if (view == iso) {
            iso.onClick();
            return false;
        }
        return false;
    }

    private void stopBulbCapture() {
        Log.d(TAG, "Stop BULB");
        bulbcapture = false;
        m_camera.cancelTakePicture();
        m_camera.startDirectShutter();
    }

    private void startBulbCapture()
    {
        m_tvHint.setVisibility(View.GONE);
        m_tvMsg.setVisibility(View.GONE);
        bulbcapture = true;
        m_camera.stopDirectShutter(new CameraEx.DirectShutterStoppedCallback() {
            @Override
            public void onShutterStopped(CameraEx cameraEx) {
                Log.d(TAG,"start Bulb");
                takePicture();
            }
        });
    }



    @Override
    public boolean onUpKeyDown()
    {
        return true;
    }

    @Override
    public boolean onUpKeyUp()
    {
        if (m_curPreviewMagnification != 0)
        {
            movePreviewVertical((int)(-500.0f / m_curPreviewMagnificationFactor));
            return true;
        }
        else
        {
            // Toggle visibility of some views
            cycleVisibleViews();
            return true;
        }
    }

    @Override
    public boolean onDownKeyDown()
    {
        return true;
    }

    @Override
    public boolean onDownKeyUp()
    {
        /*if (m_curPreviewMagnification != 0)
        {
            movePreviewVertical((int)(500.0f / m_curPreviewMagnificationFactor));
            return true;
        }
        else
        {
            switch (m_dialMode)
            {
                case shutter:
                    if (aperture.haveApertureControl())
                    {
                        setDialMode(DialMode.aperture);
                        break;
                    }
                case aperture:
                    setDialMode(DialMode.iso);
                    break;
                case iso:
                    setDialMode(DialMode.exposure);
                    break;
                case exposure:
                    setDialMode(m_haveTouchscreen ? DialMode.shutter : DialMode.mode);
                    break;
                case mode:
                    setDialMode(DialMode.drive);
                    break;
                case drive:
                    setDialMode(DialMode.timelapse);
                    break;
                case timelapse:
                    setDialMode(DialMode.bracket);
                    break;
                case bracket:
                    setDialMode(DialMode.shutter);
                    break;
            }
            return true;
        }*/
        return true;
    }

    @Override
    public boolean onLeftKeyDown()
    {
        return true;
    }

    @Override
    public boolean onLeftKeyUp()
    {
        if (m_curPreviewMagnification != 0)
        {
            movePreviewHorizontal((int)(-500.0f / m_curPreviewMagnificationFactor));
            return true;
        }
        return false;
    }

    @Override
    public boolean onRightKeyDown()
    {
        return true;
    }

    @Override
    public boolean onRightKeyUp()
    {
        if (m_curPreviewMagnification != 0)
        {
            movePreviewHorizontal((int)(500.0f / m_curPreviewMagnificationFactor));
            return true;
        }
        return false;
    }

    boolean bulbcapture = false;

    @Override
    protected boolean onShutterKeyUp()
    {
        Log.d(TAG,"onShutterKeyUp");
        m_shutterKeyDown = false;
        return false;
    }

    @Override
    protected boolean onShutterKeyDown()
    {
        Log.d(TAG,"onShutterKeyDown");
        // direct shutter...
        /*
        log("onShutterKeyDown\n");
        if (!m_takingPicture)
        {
            m_takingPicture = true;
            m_shutterKeyDown = true;
            m_camera.burstableTakePicture();
        }
        */
        return false;
    }

    @Override
    protected boolean onDeleteKeyUp()
    {
        // Exiting, make sure the app isn't restarted
        Intent intent = new Intent("com.android.server.DAConnectionManagerService.AppInfoReceive");
        intent.putExtra("package_name", getComponentName().getPackageName());
        intent.putExtra("class_name", getComponentName().getClassName());
        intent.putExtra("pullingback_key", new String[] {});
        intent.putExtra("resume_key", new String[] {});
        sendBroadcast(intent);
        onBackPressed();
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        Log.d(TAG, "onkeydown:"+event.getScanCode()+ " event:" + event.getAction());
        final int scanCode = event.getScanCode();
        if (timelapse.isActive() && scanCode != ScalarInput.ISV_KEY_ENTER)
            return true;
        // TODO: Use m_supportedPreviewMagnifications
        /*if (m_dialMode != DialMode.timelapseSetInterval && m_dialMode != DialMode.timelapseSetPicCount)
        {
            if (scanCode == ScalarInput.ISV_KEY_ZOOM_TELE && !m_zoomLeverPressed)
            {
                // zoom lever tele
                m_zoomLeverPressed = true;
                if (m_curPreviewMagnification == 0)
                {
                    m_curPreviewMagnification = 100;
                    m_lFocusScale.setVisibility(View.GONE);
                }
                else
                    m_curPreviewMagnification = 200;
                m_camera.setPreviewMagnification(m_curPreviewMagnification, m_curPreviewMagnificationPos);
                return true;
            }
            else if (scanCode == ScalarInput.ISV_KEY_ZOOM_WIDE && !m_zoomLeverPressed)
            {
                // zoom lever wide
                m_zoomLeverPressed = true;
                if (m_curPreviewMagnification == 200)
                {
                    m_curPreviewMagnification = 100;
                    m_camera.setPreviewMagnification(m_curPreviewMagnification, m_curPreviewMagnificationPos);
                }
                else
                {
                    m_curPreviewMagnification = 0;
                    m_camera.stopPreviewMagnification();
                }
                return true;
            }
            else if (scanCode == ScalarInput.ISV_KEY_ZOOM_OFF)
            {
                // zoom lever returned to neutral position
                m_zoomLeverPressed = false;
                return true;
            }
        }*/

        if (scanCode == ScalarInput.ISV_KEY_S2) {
            Log.d(TAG, "S2");
            return true;
        }
        if (scanCode == ScalarInput.ISV_KEY_S1_1)
            Log.d(TAG, "S1_1");

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d(TAG, "onkeyup:"+event.getScanCode()+ " event:" + event.getAction());
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        try
        {
            Camera cam = m_camera.getNormalCamera();
            cam.setPreviewDisplay(holder);
            cam.startPreview();
        }
        catch (IOException e)
        {
            m_tvMsg.setText("Error starting preview!");
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        //log(String.format("surfaceChanged width %d height %d\n", width, height));
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {}

    @Override
    protected void setColorDepth(boolean highQuality)
    {
        super.setColorDepth(false);
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
}
