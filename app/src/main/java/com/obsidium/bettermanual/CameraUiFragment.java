package com.obsidium.bettermanual;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.killerink.ActivityInterface;
import com.github.killerink.KeyEvents;

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
import com.obsidium.bettermanual.views.ImageStabView;
import com.obsidium.bettermanual.views.IsoView;
import com.obsidium.bettermanual.views.PreviewNavView;
import com.obsidium.bettermanual.views.ShutterView;
import com.sony.scalar.hardware.CameraEx;

import java.util.ArrayList;
import java.util.List;



public class CameraUiFragment extends Fragment implements View.OnClickListener, CameraEx.ShutterListener, CameraUiInterface, KeyEvents
{

    private static final boolean LOGGING_ENABLED = false;
    private static final int MESSAGE_TIMEOUT = 1000;
    private final  String TAG  = CameraUiFragment.class.getSimpleName();

    private int             m_pictureReviewTime;

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
    private ExposureModeView exposureMode;
    private ImageView       m_ivTimelapse;
    private ImageView       m_ivBracket;
    private GridView m_vGrid;
    private TextView        m_tvHint;
    private FocusScaleView m_focusScaleView;
    private View            m_lFocusScale;

    private LinearLayout bottomHolder;
    private LinearLayout leftHolder;

    private ImageStabView imageStabView;

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

    // Preview magnification
    private List<Integer>   m_supportedPreviewMagnifications;
    private boolean         m_zoomLeverPressed;
    private int             m_curPreviewMagnification;
    private float           m_curPreviewMagnificationFactor;
    private Pair<Integer, Integer>  m_curPreviewMagnificationPos = new Pair<Integer, Integer>(0, 0);
    private int             m_curPreviewMagnificationMaxPos;
    private PreviewNavView m_previewNavView;


    private final Runnable  m_hideMessageRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            m_tvMsg.setVisibility(View.GONE);
        }
    };

    private static final int VIEW_FLAG_GRID         = 0x01;
    private static final int VIEW_FLAG_HISTOGRAM    = 0x02;
    private static final int VIEW_FLAG_EXPOSURE     = 0x04;
    private static final int VIEW_FLAG_MASK         = 0x07; // all flags combined
    private int             m_viewFlags;
    private boolean bulbcapture = false;

    private ActivityInterface activityInterface;


    public static CameraUiFragment getCameraUiFragment(ActivityInterface activityInterface)
    {
        CameraUiFragment cu = new CameraUiFragment();
        cu.setActivityInterface(activityInterface);
        return cu;
    }

    private void setActivityInterface(ActivityInterface activityInterface) {
        this.activityInterface = activityInterface;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG,"onCreateView");
        return inflater.inflate(R.layout.camera_ui_fragment,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG,"onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        dialViews = new ArrayList<View>();
        bottomHolder = (LinearLayout)view.findViewById(R.id.bottom_holder);
        leftHolder = (LinearLayout) view.findViewById(R.id.left_holder);


        m_tvLog = (TextView)view.findViewById(R.id.tvLog);
        m_tvLog.setVisibility(LOGGING_ENABLED ? View.VISIBLE : View.GONE);

        m_vHist = (HistogramView)view.findViewById(R.id.vHist);

        m_tvMagnification = (TextView)view.findViewById(R.id.tvMagnification);

        m_previewNavView = (PreviewNavView)view.findViewById(R.id.vPreviewNav);
        m_previewNavView.setVisibility(View.GONE);

        m_tvMsg = (TextView)view.findViewById(R.id.tvMsg);

        m_vGrid = (GridView)view.findViewById(R.id.vGrid);

        m_tvHint = (TextView)view.findViewById(R.id.tvHint);
        m_tvHint.setVisibility(View.GONE);

        m_focusScaleView = (FocusScaleView)view.findViewById(R.id.vFocusScale);
        m_lFocusScale = view.findViewById(R.id.lFocusScale);
        m_lFocusScale.setVisibility(View.GONE);

        //noinspection ResourceType
        ((ImageView)view.findViewById(R.id.ivFocusRight)).setImageResource(SonyDrawables.p_16_dd_parts_rec_focuscontrol_far);
        //noinspection ResourceType
        ((ImageView)view.findViewById(R.id.ivFocusLeft)).setImageResource(SonyDrawables.p_16_dd_parts_rec_focuscontrol_near);



        timelapse = new CaptureModeTimelapse(this);
        bracket = new CaptureModeBracket(this);
    }

    private void loadUiItems() {
        exposureMode = new ExposureModeView(getContext());
        exposureMode.setOnClickListener(this);
        exposureMode.setActivity(activityInterface);
        dialViews.add(exposureMode);
        leftHolder.addView(exposureMode);

        driveMode = new DriveMode(getContext());
        driveMode.setOnClickListener(this);
        driveMode.setActivity(activityInterface);
        dialViews.add(driveMode);
        leftHolder.addView(driveMode);

        m_ivTimelapse = new ImageView(getContext());
        //noinspection ResourceType
        m_ivTimelapse.setImageResource(SonyDrawables.p_16_dd_parts_43_shoot_icon_setting_drivemode_invalid);
        m_ivTimelapse.setOnClickListener(this);
        dialViews.add(m_ivTimelapse);
        leftHolder.addView(m_ivTimelapse);

        m_ivBracket = new ImageView(getContext());
        //noinspection ResourceType
        m_ivBracket.setImageResource(SonyDrawables.p_16_dd_parts_contshot);
        m_ivBracket.setOnClickListener(this);
        dialViews.add(m_ivBracket);
        leftHolder.addView(m_ivBracket);

        if (activityInterface.getCamera().isImageStabSupported()) {
            imageStabView = new ImageStabView(getContext());
            imageStabView.setActivity(activityInterface);
            dialViews.add(imageStabView);
            leftHolder.addView(imageStabView);
        }

        final int margineright = (int)getResources().getDimension(R.dimen.bottomHolderChildMarginRight);
        m_tvShutter = new ShutterView(getContext());
        m_tvShutter.setTextSize((int)getResources().getDimension(R.dimen.textSize));
        LinearLayout.LayoutParams params= new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, margineright, 0);
        m_tvShutter.setLayoutParams(params);
        m_tvShutter.setOnTouchListener(m_tvShutter.getSwipeTouchListner());
        m_tvShutter.setCameraUiInterface(this);
        dialViews.add(m_tvShutter);
        bottomHolder.addView(m_tvShutter);

        aperture = new ApertureView(getContext());
        aperture.setTextSize((int)getResources().getDimension(R.dimen.textSize));
        aperture.setOnTouchListener(aperture.getSwipeTouchListner());
        aperture.setCameraUiInterface(this);
        aperture.setLayoutParams(params);
        dialViews.add(aperture);
        bottomHolder.addView(aperture);

        iso = new IsoView(getContext());
        iso.setTextSize((int)getResources().getDimension(R.dimen.textSize));
        iso.setOnTouchListener(iso.getSwipeTouchListner());
        iso.setCameraUiInterface(this);
        iso.setLayoutParams(params);
        dialViews.add(iso);
        bottomHolder.addView(iso);

        evCompensation = new EvView(getContext());
        evCompensation.setTextSize((int)getResources().getDimension(R.dimen.textSize));
        evCompensation.setOnTouchListener(evCompensation.getSwipeTouchListner());
        evCompensation.setCameraUiInterface(this);
        evCompensation.setLayoutParams(params);
        dialViews.add(evCompensation);
        bottomHolder.addView(evCompensation);

        m_tvExposure = new TextView(getContext());
        m_tvExposure.setTextSize((int)getResources().getDimension(R.dimen.textSize));
        m_tvExposure.setLayoutParams(params);
        //noinspection ResourceType
        m_tvExposure.setCompoundDrawablesWithIntrinsicBounds(SonyDrawables.p_meteredmanualicon, 0, 0, 0);
        bottomHolder.addView(m_tvExposure);
        setDialMode(0);
    }

    private void clearUiItems()
    {
        dialViews.clear();
        leftHolder.removeAllViews();
        bottomHolder.removeAllViews();
    }

    @Override
    public void onResume()
    {
        Log.d(TAG,"onResume");
        super.onResume();
        activityInterface.getDialHandler().setDialEventListner(this);
        loadUiItems();
        initUi();
    }

    @Override
    public void onPause()
    {
        Log.d(TAG,"onPause");
        saveDefaults();
        clearUiItems();
        if (activityInterface.getCamera().getAutoPictureReviewControls() != null)
            activityInterface.getCamera().getAutoPictureReviewControls().setPictureReviewTime(m_pictureReviewTime);
        //activityInterface.getCamera().setAutoPictureReviewControl(null);
        super.onPause();
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
                activityInterface.getCamera().setPreviewMagnification(m_curPreviewMagnification, m_curPreviewMagnificationPos);
                return true;
            }
            return false;
        }
    }

    public void showMessageDelayed(String msg)
    {
        showMessage(msg);
        activityInterface.getMainHandler().removeCallbacks(m_hideMessageRunnable);
        activityInterface.getMainHandler().postDelayed(m_hideMessageRunnable, MESSAGE_TIMEOUT);
    }

    public void showMessage(final String msg)
    {
        activityInterface.getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                m_tvMsg.setText(msg);
                m_tvMsg.setVisibility(View.VISIBLE);
            }
        });

    }

    public void hideMessage()
    {
        activityInterface.getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                m_tvMsg.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public void showHintMessage(final String msg) {
        activityInterface.getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                m_tvHint.setText(msg);
                m_tvHint.setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    public void hideHintMessage() {
        activityInterface.getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                m_tvHint.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public int getActiveViewsFlag() {
        return m_viewFlags;
    }

    @Override
    public void setActiveViewFlag(int viewsToShow) {
        m_viewFlags = viewsToShow;
    }

    @Override
    public ExposureModeView getExposureMode() {
        return exposureMode;
    }

    private void log(final String str)
    {
        if (LOGGING_ENABLED)
            m_tvLog.append(str);
    }


    @Override
    public ActivityInterface getActivityInterface() {
        return activityInterface;
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
        if (!bulbcapture) {

            activityInterface.getCamera().cancelTakePicture();
            if (timelapse.isActive())
                timelapse.onShutter(i);
            else if (bracket.isActive())
                bracket.onShutter(i);
        }
    }


    public void updateViewVisibility()
    {
        activityInterface.getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                m_vHist.setVisibility((m_viewFlags & VIEW_FLAG_HISTOGRAM) != 0 ? View.VISIBLE : View.GONE);
                m_vGrid.setVisibility((m_viewFlags & VIEW_FLAG_GRID) != 0 ? View.VISIBLE : View.GONE);
            }
        });

    }

    private void cycleVisibleViews()
    {
        if (++m_viewFlags > VIEW_FLAG_MASK)
            m_viewFlags = 0;
        updateViewVisibility();
    }


    private void togglePreviewMagnificationViews(final boolean magnificationActive)
    {
        activityInterface.getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                m_previewNavView.setVisibility(magnificationActive ? View.VISIBLE : View.GONE);
                m_tvMagnification.setVisibility(magnificationActive ? View.VISIBLE : View.GONE);
                m_vHist.setVisibility(magnificationActive ? View.GONE : View.VISIBLE);
                setLeftViewVisibility(!magnificationActive);
            }
        });

    }



    private void saveDefaults()
    {
        // Scene mode
        activityInterface.getPreferences().setSceneMode(activityInterface.getCamera().getSceneMode());
        // Drive mode and burst speed
        activityInterface.getPreferences().setDriveMode(activityInterface.getCamera().getDriveMode());
        activityInterface.getPreferences().setBurstDriveSpeed(activityInterface.getCamera().getBurstDriveSpeed());
        // View visibility
        activityInterface.getPreferences().setViewFlags(m_viewFlags);

        // TODO: Dial mode
    }


    private void loadDefaults()
    {

        //Log.d(TAG,"Parameters: " + params.flatten());
        // Focus mode
        activityInterface.getCamera().setFocusMode(CameraEx.ParametersModifier.FOCUS_MODE_MANUAL);
        // Scene mode
        final String sceneMode = activityInterface.getPreferences().getSceneMode();
        activityInterface.getCamera().setSceneMode(sceneMode);
        // Drive mode and burst speed
        activityInterface.getCamera().setDriveMode(activityInterface.getPreferences().getDriveMode());
        activityInterface.getCamera().setBurstDriveSpeed(activityInterface.getPreferences().getBurstDriveSpeed());
        // Minimum shutter speed
        if(activityInterface.getCamera().isAutoShutterSpeedLowLimitSupported()) {
            if (sceneMode.equals(CameraEx.ParametersModifier.SCENE_MODE_MANUAL_EXPOSURE))
                activityInterface.getCamera().setAutoShutterSpeedLowLimit(-1);
            else
                activityInterface.getCamera().setAutoShutterSpeedLowLimit(activityInterface.getPreferences().getMinShutterSpeed());
        }
        // Disable self timer
        activityInterface.getCamera().setSelfTimer(0);
        // Force aspect ratio to 3:2
        activityInterface.getCamera().setImageAspectRatio(CameraEx.ParametersModifier.IMAGE_ASPECT_RATIO_3_2);
        // View visibility
        m_viewFlags = activityInterface.getPreferences().getViewFlags(VIEW_FLAG_GRID | VIEW_FLAG_HISTOGRAM);
        // TODO: Dial mode?
        setDialMode(0);

        if (activityInterface.getCamera().isSupportedLongExposureNR())
            activityInterface.getCamera().setLongExposureNR(false);
    }


    private void initUi() {

        // Disable picture review
        m_pictureReviewTime = activityInterface.getCamera().getAutoPictureReviewControls().getPictureReviewTime();
        activityInterface.getCamera().getAutoPictureReviewControls().setPictureReviewTime(0);

        m_vGrid.setVideoRect(activityInterface.getDisplayManager().getDisplayedVideoRect());

        // Exposure compensation
        evCompensation.init(activityInterface.getCamera());

        // Preview/Histogram
        activityInterface.getCamera().setPreviewAnalizeListener(new CameraEx.PreviewAnalizeListener()
        {
            @Override
            public void onAnalizedData(CameraEx.AnalizedData analizedData, CameraEx cameraEx)
            {
                if (analizedData != null && analizedData.hist != null && analizedData.hist.Y != null && m_vHist.getVisibility() == View.VISIBLE)
                    m_vHist.setHistogram(analizedData.hist.Y);
            }
        });


        // ISO
        activityInterface.getCamera().setAutoISOSensitivityListener(iso);
        iso.init(activityInterface.getCamera());

        // Shutter
        activityInterface.getCamera().setShutterSpeedChangeListener(bracket);

        //returns when a capture is done, seems to replace the default android camera1 api CaptureCallback that get called with Camera.takePicture(shutter,raw, jpeg)
        //also it seems Camera.takePicture is nonfunctional/crash on a6000
        activityInterface.getCamera().setShutterListener(this);

        //m_camera.setJpegListener(); maybe is used to get jpeg/raw data returned

        // Aperture
        activityInterface.getCamera().setApertureChangeListener(aperture);

        // Exposure metering
        activityInterface.getCamera().setProgramLineRangeOverListener(new CameraEx.ProgramLineRangeOverListener()
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

        aperture.setText(String.format("f%.1f", (float) activityInterface.getCamera().getAperture() / 100.0f));

        Pair<Integer, Integer> sp = activityInterface.getCamera().getShutterSpeed();
        m_tvShutter.updateShutterSpeed(sp.first, sp.second);

        m_supportedPreviewMagnifications = (List<Integer>) activityInterface.getCamera().getSupportedPreviewMagnification();
        activityInterface.getCamera().setPreviewMagnificationListener(new CameraEx.PreviewMagnificationListener()
        {
            @Override
            public void onChanged(boolean enabled, int magFactor, int magLevel, Pair coords, CameraEx cameraEx)
            {
                // magnification / 100 = x.y
                // magLevel = value passed to setPreviewMagnification
                //*
                m_tvLog.setText("onChanged enabled:" + String.valueOf(enabled) + " magFactor:" + String.valueOf(magFactor) + " magLevel:" +
                    String.valueOf(magLevel) + " x:" + coords.first + " y:" + coords.second + "\n");
                //*
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
                //*
                /*log("onInfoUpdated b:" + String.valueOf(b) +
                               " x:" + coords.first + " y:" + coords.second + "\n");*/
                //*
            }
        });

        activityInterface.getCamera().setFocusDriveListener(new CameraEx.FocusDriveListener()
        {
            @Override
            public void onChanged(final CameraEx.FocusPosition focusPosition, CameraEx cameraEx)
            {
                if (m_curPreviewMagnification == 0)
                {
                    m_lFocusScale.setVisibility(View.VISIBLE);
                    m_focusScaleView.setMaxPosition(focusPosition.maxPosition);
                    m_focusScaleView.setCurPosition(focusPosition.currentPosition);
                    activityInterface.getMainHandler().removeCallbacks(m_hideFocusScaleRunnable);
                    activityInterface.getMainHandler().postDelayed(m_hideFocusScaleRunnable, 2000);
                }
            }
        });

        loadDefaults();
        driveMode.updateImage();
        exposureMode.updateImage();
        imageStabView.updateImage();
        updateViewVisibility();
    }

    // OnClickListener
    public void onClick(View view)
    {
        if(view instanceof BaseImageView)
            ((BaseImageView) view).toggle();
        else
            if (view.equals(timelapse))
                timelapse.prepare();
            else if (view.equals(bracket))
                bracket.prepare();
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

    private void setDialMode(int mode)
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


    private void movePreviewVertical(int delta)
    {
        int newY = m_curPreviewMagnificationPos.second + delta;
        if (newY > m_curPreviewMagnificationMaxPos)
            newY = m_curPreviewMagnificationMaxPos;
        else if (newY < -m_curPreviewMagnificationMaxPos)
            newY = -m_curPreviewMagnificationMaxPos;
        m_curPreviewMagnificationPos = new Pair<Integer, Integer>(m_curPreviewMagnificationPos.first, newY);
        activityInterface.getCamera().setPreviewMagnification(m_curPreviewMagnification, m_curPreviewMagnificationPos);
    }

    private void movePreviewHorizontal(int delta)
    {
        int newX = m_curPreviewMagnificationPos.first + delta;
        if (newX > m_curPreviewMagnificationMaxPos)
            newX = m_curPreviewMagnificationMaxPos;
        else if (newX < -m_curPreviewMagnificationMaxPos)
            newX = -m_curPreviewMagnificationMaxPos;
        m_curPreviewMagnificationPos = new Pair<Integer, Integer>(newX, m_curPreviewMagnificationPos.second);
        activityInterface.getCamera().setPreviewMagnification(m_curPreviewMagnification, m_curPreviewMagnificationPos);
    }

    @Override
    public boolean onEnterKeyUp()
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
            activityInterface.getCamera().setPreviewMagnification(m_curPreviewMagnification, m_curPreviewMagnificationPos);
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
            activityInterface.getDialHandler().setDialEventListner(timelapse);
            timelapse.onEnterKeyDown();

            return false;
        }
        else if (view == m_ivBracket)
        {
            activityInterface.getDialHandler().setDialEventListner(bracket);
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
        else if (view == imageStabView)
        {
            imageStabView.toggle();
        }
        else if (view == iso) {
            iso.onClick();
            return false;
        }
        return true;
    }

    @Override
    public boolean onFnKeyDown() {
        return false;
    }

    @Override
    public boolean onFnKeyUp() {
        return false;
    }

    @Override
    public boolean onAelKeyDown() {
        return false;
    }

    @Override
    public boolean onAelKeyUp() {
        return false;
    }

    @Override
    public boolean onMenuKeyDown() {
        return false;
    }

    @Override
    public boolean onMenuKeyUp() {
        return false;
    }

    @Override
    public boolean onFocusKeyDown() {
        return false;
    }

    @Override
    public boolean onFocusKeyUp() {
        return false;
    }

    @Override
    public boolean onEnterKeyDown()
    {

        return false;
    }

    private void stopBulbCapture() {
        Log.d(TAG, "Stop BULB");
        bulbcapture = false;
        activityInterface.getCamera().cancelTakePicture();
    }

    private void startBulbCapture()
    {
        m_tvHint.setVisibility(View.GONE);
        m_tvMsg.setVisibility(View.GONE);
        bulbcapture = true;
        activityInterface.getCamera().takePicture();
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



    @Override
    public boolean onShutterKeyUp()
    {
        Log.d(TAG,"onShutterKeyUp");
        return false;
    }

    @Override
    public boolean onPlayKeyDown() {
        return false;
    }

    @Override
    public boolean onPlayKeyUp() {
        return false;
    }

    @Override
    public boolean onMovieKeyDown() {
        return false;
    }

    @Override
    public boolean onMovieKeyUp() {
        return false;
    }

    @Override
    public boolean onC1KeyDown() {
        return false;
    }

    @Override
    public boolean onC1KeyUp() {
        return false;
    }

    @Override
    public boolean onLensAttached() {
        return false;
    }

    @Override
    public boolean onLensDetached() {
        return false;
    }

    @Override
    public boolean onModeDialChanged(int value) {
        return false;
    }

    @Override
    public boolean onZoomTeleKey() {
        return false;
    }

    @Override
    public boolean onZoomWideKey() {
        return false;
    }

    @Override
    public boolean onZoomOffKey() {
        return false;
    }

    @Override
    public boolean onDeleteKeyDown() {
        return false;
    }

    @Override
    public boolean onShutterKeyDown()
    {
        Log.d(TAG,"onShutterKeyDown");
        return false;
    }

    @Override
    public boolean onDeleteKeyUp()
    {
        // Exiting, make sure the app isn't restarted
        activityInterface.closeApp();
        return true;
    }

    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        Log.d(TAG, "onkeydown:"+event.getScanCode()+ " event:" + event.getAction());
        final int scanCode = event.getScanCode();
        if (timelapse.isActive() && scanCode != ScalarInput.ISV_KEY_ENTER)
            return true;
        // TODO: Use m_supportedPreviewMagnifications
        *//*if (m_dialMode != DialMode.timelapseSetInterval && m_dialMode != DialMode.timelapseSetPicCount)
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
        }*//*

        if (scanCode == ScalarInput.ISV_KEY_S2) {
            Log.d(TAG, "S2");
            return true;
        }
        if (scanCode == ScalarInput.ISV_KEY_S1_1)
            Log.d(TAG, "S1_1");

        return super.onKeyDown(keyCode, event);
    }*/

}
