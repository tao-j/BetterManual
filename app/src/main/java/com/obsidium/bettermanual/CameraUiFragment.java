package com.obsidium.bettermanual;

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
import com.github.killerink.MainActivity;
import com.github.killerink.TimeLog;
import com.github.killerink.camera.CaptureSession;
import com.obsidium.bettermanual.capture.CaptureModeBracket;
import com.obsidium.bettermanual.capture.CaptureModeTimelapse;
import com.obsidium.bettermanual.views.ApertureView;
import com.obsidium.bettermanual.views.BaseImageView;
import com.obsidium.bettermanual.views.BaseTextView;
import com.obsidium.bettermanual.views.DialViewInterface;
import com.obsidium.bettermanual.views.DriveMode;
import com.obsidium.bettermanual.views.EvView;
import com.obsidium.bettermanual.views.ExposureModeView;
import com.obsidium.bettermanual.views.FocusScaleView;
import com.obsidium.bettermanual.views.GridView;
import com.obsidium.bettermanual.views.HistogramView;
import com.obsidium.bettermanual.views.ImageStabView;
import com.obsidium.bettermanual.views.IsoView;
import com.obsidium.bettermanual.views.LongExpoNR;
import com.obsidium.bettermanual.views.ShutterView;
import com.sony.scalar.hardware.CameraEx;
import com.sony.scalar.provider.AvindexStore;

import java.util.ArrayList;
import java.util.List;



public class CameraUiFragment extends Fragment implements View.OnClickListener,
        CameraUiInterface, KeyEvents, CameraEx.PreviewAnalizeListener,CameraEx.ProgramLineRangeOverListener,
        CameraEx.FocusDriveListener
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
    private TextView        m_tvMsg;
    private HistogramView m_vHist;
    private DriveMode driveMode;
    private ExposureModeView exposureMode;
    private BaseImageView       m_ivTimelapse;
    private BaseImageView       m_ivBracket;
    private GridView m_vGrid;
    private TextView        m_tvHint;
    private FocusScaleView m_focusScaleView;
    private View            m_lFocusScale;
    private LongExpoNR longExpoNR;

    private LinearLayout bottomHolder;
    private LinearLayout leftHolder;

    private ImageStabView imageStabView;

    private List<DialViewInterface> dialViews;
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
        final TimeLog timeLog = new TimeLog("onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        dialViews = new ArrayList<DialViewInterface>();
        bottomHolder = (LinearLayout)view.findViewById(R.id.bottom_holder);
        leftHolder = (LinearLayout) view.findViewById(R.id.left_holder);

        m_tvLog = (TextView)view.findViewById(R.id.tvLog);
        m_tvLog.setVisibility(LOGGING_ENABLED ? View.VISIBLE : View.GONE);

        m_vHist = (HistogramView)view.findViewById(R.id.vHist);

        m_tvMsg = (TextView)view.findViewById(R.id.tvMsg);

        m_vGrid = (GridView)view.findViewById(R.id.vGrid);

        m_tvHint = (TextView)view.findViewById(R.id.tvHint);
        m_tvHint.setVisibility(View.GONE);

        m_focusScaleView = (FocusScaleView)view.findViewById(R.id.vFocusScale);
        m_lFocusScale = view.findViewById(R.id.lFocusScale);
        m_lFocusScale.setVisibility(View.GONE);

        //noinspection ResourceType
        ((ImageView)view.findViewById(R.id.ivFocusRight)).setImageResource(getResources().getInteger(R.integer.p_16_dd_parts_rec_focuscontrol_far));
        //noinspection ResourceType
        ((ImageView)view.findViewById(R.id.ivFocusLeft)).setImageResource(getResources().getInteger(R.integer.p_16_dd_parts_rec_focuscontrol_near));

        timelapse = new CaptureModeTimelapse(this);
        bracket = new CaptureModeBracket(this);
        timeLog.logTime();
    }

    @Override
    public void onResume()
    {
        Log.d(TAG,"onResume");
        final TimeLog timeLog = new TimeLog("onResume");
        super.onResume();
        activityInterface.getMainHandler().post(setupUiItems);
        timeLog.logTime();
    }

    private Runnable setupUiItems = new Runnable() {
        @Override
        public void run() {
            //first add ui
            loadUiItems();
            initUi();
            //then set the key event listner to avoid nullpointer
            activityInterface.getDialHandler().setDialEventListner(CameraUiFragment.this);
        }
    };

    @Override
    public void onPause()
    {
        Log.d(TAG,"onPause");
        //save View visibility
        activityInterface.getPreferences().setViewFlags(m_viewFlags);
        activityInterface.getPreferences().setDialMode(lastDialView);

        clearUiItems();
        if (activityInterface.getCamera().getAutoPictureReviewControls() != null)
            activityInterface.getCamera().getAutoPictureReviewControls().setPictureReviewTime(m_pictureReviewTime);
        super.onPause();
    }

    private void loadUiItems() {
        final TimeLog timeLog = new TimeLog("loadUiItems");
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

        m_ivTimelapse = new BaseImageView(getContext()) {
            @Override
            public void updateImage() {

            }

            @Override
            public void toggle() {
                if (timelapse.isActive())
                    timelapse.abort();
                else {
                    activityInterface.getDialHandler().setDialEventListner(timelapse);
                    timelapse.onEnterKeyUp();
                }
            }

            @Override
            public void setIn_DecrementValue(int value) {

            }

            @Override
            public String getNavigationString() {
                return getString(R.string.view_startBracket_Timelapse);
            }
        };
        //noinspection ResourceType
        m_ivTimelapse.setImageResource(getResources().getInteger(R.integer.p_16_dd_parts_43_shoot_icon_setting_drivemode_invalid));
        m_ivTimelapse.setOnClickListener(this);
        dialViews.add(m_ivTimelapse);
        leftHolder.addView(m_ivTimelapse);

        m_ivBracket = new BaseImageView(getContext()) {
            @Override
            public void updateImage() {

            }

            @Override
            public void toggle() {
                if (bracket.isActive())
                {
                    bracket.abort();
                }
                else {
                    activityInterface.getDialHandler().setDialEventListner(bracket);
                    bracket.onEnterKeyUp();
                }
            }

            @Override
            public void setIn_DecrementValue(int value) {

            }

            @Override
            public String getNavigationString() {
                return getString(R.string.view_startBracket_Timelapse);
            }
        };
        //noinspection ResourceType
        m_ivBracket.setImageResource(getResources().getInteger(R.integer.p_16_dd_parts_contshot));
        m_ivBracket.setOnClickListener(this);
        dialViews.add(m_ivBracket);
        leftHolder.addView(m_ivBracket);

        if (activityInterface.getCamera().isImageStabSupported()) {
            imageStabView = new ImageStabView(getContext());
            imageStabView.setActivity(activityInterface);
            imageStabView.setOnClickListener(this);
            dialViews.add(imageStabView);
            leftHolder.addView(imageStabView);
        }

        if (activityInterface.getCamera().isLongExposureNoiseReductionSupported())
        {
            longExpoNR = new LongExpoNR(getContext());
            longExpoNR.setActivity(activityInterface);
            longExpoNR.setOnClickListener(this);
            dialViews.add(longExpoNR);
            leftHolder.addView(longExpoNR);
        }

        final int margineright = (int)getResources().getDimension(R.dimen.bottomHolderChildMarginRight);
        LinearLayout.LayoutParams params= new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, margineright, 0);
        params.weight = 1;

        m_tvShutter = new ShutterView(getContext());
        m_tvShutter.setTextSize((int)getResources().getDimension(R.dimen.textSize));
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
        m_tvExposure.setCompoundDrawablesWithIntrinsicBounds(getResources().getInteger(R.integer.p_meteredmanualicon), 0, 0, 0);
        bottomHolder.addView(m_tvExposure);
        setDialMode(0);

        timeLog.logTime();
    }

    private void initUi() {

        Log.d(TAG,"initUi");
        final TimeLog timeLog = new TimeLog("initUi");
        // Disable picture review
        m_pictureReviewTime = activityInterface.getCamera().getAutoPictureReviewControls().getPictureReviewTime();
        activityInterface.getCamera().getAutoPictureReviewControls().setPictureReviewTime(0);

        m_vGrid.setVideoRect(activityInterface.getDisplayManager().getDisplayedVideoRect());

        // Exposure compensation
        evCompensation.init(activityInterface.getCamera());

        // Preview/Histogram
        activityInterface.getCamera().setPreviewAnalizeListener(this);


        // ISO
        activityInterface.getCamera().setAutoISOSensitivityListener(iso);
        iso.init(activityInterface.getCamera());

        // Shutter
        activityInterface.getCamera().setShutterSpeedChangeListener(bracket);

        //returns when a capture is done, seems to replace the default android camera1 api CaptureCallback that get called with Camera.takePicture(shutter,raw, jpeg)
        //also it seems Camera.takePicture is nonfunctional/crash on a6000
        //activityInterface.getCamera().setShutterListener(this);

        //m_camera.setJpegListener(); maybe is used to get jpeg/raw data returned

        // Aperture
        activityInterface.getCamera().setApertureChangeListener(aperture);

        // Exposure metering
        activityInterface.getCamera().setProgramLineRangeOverListener(this);

        aperture.setText(String.format("f%.1f", (float) activityInterface.getCamera().getAperture() / 100.0f));

        Pair<Integer, Integer> sp = activityInterface.getCamera().getShutterSpeed();
        m_tvShutter.updateShutterSpeed(sp.first, sp.second);

        activityInterface.getCamera().setFocusDriveListener(this);

        m_viewFlags = activityInterface.getPreferences().getViewFlags(VIEW_FLAG_GRID | VIEW_FLAG_HISTOGRAM);
        setDialMode(activityInterface.getPreferences().getDialMode(0));

        driveMode.updateImage();
        exposureMode.updateImage();
        if (imageStabView != null)
            imageStabView.updateImage();
        if (longExpoNR != null)
            longExpoNR.updateImage();
        updateViewVisibility();

        Log.d(TAG,"initUiEnd");
        timeLog.logTime();
    }

    private void clearUiItems()
    {
        dialViews.clear();
        leftHolder.removeAllViews();
        bottomHolder.removeAllViews();
    }



    /* ##############################################################################
       ###### CameraUiInterface impl ###
       #################################  */

    @Override
    public void showMessageDelayed(String msg)
    {
        showMessage(msg);
        activityInterface.getMainHandler().removeCallbacks(m_hideMessageRunnable);
        activityInterface.getMainHandler().postDelayed(m_hideMessageRunnable, MESSAGE_TIMEOUT);
    }

    @Override
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

    @Override
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

    @Override
    public ActivityInterface getActivityInterface() {
        return activityInterface;
    }

    @Override
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

    @Override
    public void setLeftViewVisibility(boolean visible)
    {
        final int visibility = visible ? View.VISIBLE : View.GONE;
        leftHolder.setVisibility(visibility);
    }

    private void cycleVisibleViews()
    {
        if (++m_viewFlags > VIEW_FLAG_MASK)
            m_viewFlags = 0;
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

    private void setDialMode(int mode)
    {
        DialViewInterface lastView = dialViews.get(lastDialView);
        if (lastView == null)
            return;
        lastView.setColorToView(Color.WHITE);
        lastDialView = lastDialView + mode;
        if (lastDialView >= dialViews.size())
            lastDialView = 0;
        else if(lastDialView < 0)
            lastDialView = dialViews.size()-1;

        lastView = dialViews.get(lastDialView);
        lastView.setColorToView(Color.GREEN);
        showHintMessage(lastView.getNavigationString());
    }




    /*  ##################################################################
        ## Key events impl ##
        ##################### */

    @Override
    public boolean onUpperDialChanged(int value)
    {
        DialViewInterface view = (DialViewInterface) dialViews.get(lastDialView);
        view.setIn_DecrementValue(value);

        return true;
    }

    @Override
    public boolean onLowerDialChanged(int value) {

        setDialMode(value);

        return true;
    }

    @Override
    public boolean onEnterKeyUp()
    {
        DialViewInterface view = dialViews.get(lastDialView);
        Log.d(TAG,"onEnterKeyDown");
        if (view instanceof BaseImageView)
            ((BaseImageView) view).toggle();
        else if (view instanceof BaseTextView)
            ((BaseTextView) view).onClick();
        showHintMessage(view.getNavigationString());
        return true;
    }

    @Override
    public boolean onFnKeyDown() {
        return false;
    }

    @Override
    public boolean onFnKeyUp() {
        activityInterface.getBackHandler().post(new Runnable() {
            @Override
            public void run() {
                activityInterface.getCamera().cancleCapture();
            }
        });

        return false;
    }

    @Override
    public boolean onAelKeyDown() {
        return false;
    }

    @Override
    public boolean onAelKeyUp() {
        activityInterface.loadFragment(MainActivity.FRAGMENT_PREVIEWMAGNIFICATION);
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

    @Override
    public boolean onUpKeyDown()
    {
        return true;
    }

    @Override
    public boolean onUpKeyUp()
    {
            // Toggle visibility of some views
            cycleVisibleViews();
            return true;
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
        activityInterface.loadFragment(MainActivity.FRAGMENT_IMAGEVIEW);
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


    /*  ##################################################################
        ## CameraEx Events impl ##
        ########################## */

    //################# CameraEx.ShutterListener END#############

    //###################### CameraEx.PreviewAnalizeListener #################
    @Override
    public void onAnalizedData(final CameraEx.AnalizedData analizedData, CameraEx cameraEx) {
        activityInterface.getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                if (analizedData != null && analizedData.hist != null && analizedData.hist.Y != null && m_vHist.getVisibility() == View.VISIBLE)
                    m_vHist.setHistogram(analizedData.hist.Y);
            }
        });

    }
    //############### CameraEx.PreviewAnalizeListener END###############


    //###############CameraEx.ProgramLineRangeOverListener##############
    @Override
    public void onAERange(boolean b, boolean b1, boolean b2, CameraEx cameraEx) {

    }

    @Override
    public void onEVRange(int ev, CameraEx cameraEx) {
        final String text;
        if (ev == 0)
            text = "\u00B10.0";
        else if (ev > 0)
            text = String.format("+%.1f", (float)ev / 3.0f);
        else
            text = String.format("%.1f", (float)ev / 3.0f);
        m_tvExposure.post(new Runnable() {
            @Override
            public void run() {
                m_tvExposure.setText(text);
            }
        });

    }

    @Override
    public void onMeteringRange(boolean b, CameraEx cameraEx) {

    }
    //############CameraEx.ProgramLineRangeOverListener END ###########


    //##########CameraEx.FocusDriveListner################
    @Override
    public void onChanged(final CameraEx.FocusPosition focusPosition, CameraEx cameraEx) {

        activityInterface.getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                m_lFocusScale.setVisibility(View.VISIBLE);
                m_focusScaleView.setMaxPosition(focusPosition.maxPosition);
                m_focusScaleView.setCurPosition(focusPosition.currentPosition);
                activityInterface.getMainHandler().removeCallbacks(m_hideFocusScaleRunnable);
                activityInterface.getMainHandler().postDelayed(m_hideFocusScaleRunnable, 2000);
            }
        });


    }
    //##########CameraEx.FocusDriveListner ENDs################

}
