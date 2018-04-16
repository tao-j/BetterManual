package com.obsidium.bettermanual.layout;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.obsidium.bettermanual.ActivityInterface;
import com.obsidium.bettermanual.MainActivity;
import com.obsidium.bettermanual.Preferences;
import com.obsidium.bettermanual.R;
import com.obsidium.bettermanual.TimeLog;
import com.obsidium.bettermanual.camera.CameraInstance;
import com.obsidium.bettermanual.capture.CaptureModeBracket;
import com.obsidium.bettermanual.capture.CaptureModeTimelapse;
import com.obsidium.bettermanual.controller.ApertureController;
import com.obsidium.bettermanual.controller.Controller;
import com.obsidium.bettermanual.controller.DriveModeController;
import com.obsidium.bettermanual.controller.ExposureCompensationController;
import com.obsidium.bettermanual.controller.ExposureHintController;
import com.obsidium.bettermanual.controller.ExposureModeController;
import com.obsidium.bettermanual.controller.ImageStabilisationController;
import com.obsidium.bettermanual.controller.IsoController;
import com.obsidium.bettermanual.controller.LongExposureNoiseReductionController;
import com.obsidium.bettermanual.controller.ShutterController;
import com.obsidium.bettermanual.views.FocusScaleView;
import com.obsidium.bettermanual.views.GridView;
import com.obsidium.bettermanual.views.HistogramView;
import com.sony.scalar.hardware.CameraEx;

import java.util.ArrayList;
import java.util.List;


public class CameraUiFragment extends BaseLayout implements View.OnClickListener,
        CameraUiInterface, CameraEx.PreviewAnalizeListener,
        CameraEx.FocusDriveListener
{



    private static final boolean LOGGING_ENABLED = false;
    private static final int MESSAGE_TIMEOUT = 1000;
    private final  String TAG  = CameraUiFragment.class.getSimpleName();

    private int             m_pictureReviewTime;

    private TextView        m_tvExposure;
    private TextView        m_tvLog;
    private TextView        m_tvMsg;
    private HistogramView m_vHist;
    private ImageView driveMode;
    //private ExposureModeView exposureMode;
    private ImageView       m_ivTimelapse;
    private ImageView       m_ivBracket;
    private GridView m_vGrid;
    private TextView        m_tvHint;
    private FocusScaleView m_focusScaleView;
    private View            m_lFocusScale;

    private LinearLayout bottomHolder;
    private LinearLayout leftHolder;

    //private ImageStabView imageStabView;

    private List<Controller> dialViews;
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

    public CameraUiFragment(Context context, ActivityInterface activityInterface)
    {
        super(context,activityInterface);
        inflateLayout(R.layout.camera_ui_fragment);
        this.activityInterface = activityInterface;

        dialViews = new ArrayList();
        bottomHolder = (LinearLayout)findViewById(R.id.bottom_holder);
        leftHolder = (LinearLayout)findViewById(R.id.left_holder);

        m_tvLog = (TextView)findViewById(R.id.tvLog);
        m_tvLog.setVisibility(LOGGING_ENABLED ? View.VISIBLE : View.GONE);

        m_vHist = (HistogramView)findViewById(R.id.vHist);

        m_tvMsg = (TextView)findViewById(R.id.tvMsg);

        m_vGrid = (GridView)findViewById(R.id.vGrid);

        m_tvHint = (TextView)findViewById(R.id.tvHint);
        m_tvHint.setVisibility(View.GONE);

        m_focusScaleView = (FocusScaleView)findViewById(R.id.vFocusScale);
        m_lFocusScale = findViewById(R.id.lFocusScale);
        m_lFocusScale.setVisibility(View.GONE);

        //noinspection ResourceType
        ((ImageView)findViewById(R.id.ivFocusRight)).setImageResource(getResources().getInteger(R.integer.p_16_dd_parts_rec_focuscontrol_far));
        //noinspection ResourceType
        ((ImageView)findViewById(R.id.ivFocusLeft)).setImageResource(getResources().getInteger(R.integer.p_16_dd_parts_rec_focuscontrol_near));

        m_ivBracket = (ImageView)findViewById(R.id.iv_bracket);
        m_ivBracket.setImageResource(getResources().getInteger(R.integer.p_16_dd_parts_contshot));
        m_ivTimelapse = (ImageView)findViewById(R.id.iv_timelapse);
        m_ivTimelapse.setImageResource(getResources().getInteger(R.integer.p_16_dd_parts_43_shoot_icon_setting_drivemode_invalid));

        ExposureModeController.GetInstance().bindView((ImageView) findViewById(R.id.iv_exposuremode));
        dialViews.add(ExposureModeController.GetInstance());

        DriveModeController.GetInstance().bindView((ImageView)findViewById(R.id.iv_drivemode));
        dialViews.add(DriveModeController.GetInstance());

        bracket = new CaptureModeBracket(this);
        bracket.bindView((ImageView)findViewById(R.id.iv_bracket));
        dialViews.add(bracket);

        timelapse = new CaptureModeTimelapse(this);
        timelapse.bindView(((ImageView)findViewById(R.id.iv_timelapse)));
        dialViews.add(timelapse);

        ImageStabilisationController.GetInstance().bindView((ImageView) findViewById(R.id.iv_imagestab));
        dialViews.add(ImageStabilisationController.GetInstance());

        LongExposureNoiseReductionController.GetIntance().bindView((ImageView)findViewById(R.id.iv_longexponr));
        dialViews.add(LongExposureNoiseReductionController.GetIntance());

        ShutterController.GetInstance().bindView((TextView)findViewById(R.id.shutter_txt));
        dialViews.add(ShutterController.GetInstance());

        ApertureController.GetInstance().bindView((TextView)findViewById(R.id.aperture_txt));
        dialViews.add(ApertureController.GetInstance());

        IsoController.GetInstance().bindView((TextView)findViewById(R.id.iso_txt));
        dialViews.add(IsoController.GetInstance());

        ExposureCompensationController.GetInstance().bindView((TextView)findViewById(R.id.evcopmensation_txt));
        dialViews.add(ExposureCompensationController.GetInstance());

        TextView m_tvExposure = (TextView) findViewById(R.id.evhint_txt);
        m_tvExposure.setCompoundDrawablesWithIntrinsicBounds(getResources().getInteger(R.integer.p_meteredmanualicon), 0, 0, 0);
        ExposureHintController.GetInstance().bindView(m_tvExposure);
        //dialViews.add(ExposureHintController.GetInstance());

                //then set the key event listner to avoid nullpointer
        activityInterface.getDialHandler().setDialEventListner(CameraUiFragment.this);

        m_vGrid.setVideoRect(activityInterface.getDisplayManager().getDisplayedVideoRect());

        // Preview/Histogram
        CameraInstance.GET().setPreviewAnalizeListener(this);

        //returns when a capture is done, seems to replace the default android camera1 api CaptureCallback that get called with Camera.takePicture(shutter,raw, jpeg)
        //also it seems Camera.takePicture is nonfunctional/crash on a6000
        //activityInterface.getCamera().setShutterListener(this);

        //m_camera.setJpegListener(); maybe is used to get jpeg/raw data returned


        CameraInstance.GET().setFocusDriveListener(this);

        m_viewFlags = Preferences.GET().getViewFlags(VIEW_FLAG_GRID | VIEW_FLAG_HISTOGRAM);
        activityInterface.getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                setDialMode(Preferences.GET().getDialMode(0));
            }
        });


        updateViewVisibility();

        Log.d(TAG,"initUiEnd");
    }

    public void Destroy()
    {
        Preferences.GET().setViewFlags(m_viewFlags);
        Preferences.GET().setDialMode(lastDialView);

        clearUiItems();

        ApertureController.GetInstance().bindView(null);
        ShutterController.GetInstance().bindView(null);
        IsoController.GetInstance().bindView(null);
        ExposureCompensationController.GetInstance().bindView(null);
        ExposureHintController.GetInstance().bindView(null);
        ExposureModeController.GetInstance().bindView(null);
        DriveModeController.GetInstance().bindView(null);
        ImageStabilisationController.GetInstance().bindView(null);
        LongExposureNoiseReductionController.GetIntance().bindView(null);
    }


   /* private void loadUiItems() {
        final TimeLog timeLog = new TimeLog("loadUiItems");

       *//* if (CameraInstance.GET().isImageStabSupported()) {
            activityInterface.getMainHandler().post(addImageStabRunnable);
        }*//*

        if (CameraInstance.GET().isLongExposureNoiseReductionSupported()) {
            activityInterface.getMainHandler().post(addLongExpoNoiseReductionRunnable);
        }

        timeLog.logTime();
    }*/

    private void initUi() {

        Log.d(TAG,"initUi");
        final TimeLog timeLog = new TimeLog("initUi");

        m_vGrid.setVideoRect(activityInterface.getDisplayManager().getDisplayedVideoRect());

        // Preview/Histogram
        CameraInstance.GET().setPreviewAnalizeListener(this);

        //returns when a capture is done, seems to replace the default android camera1 api CaptureCallback that get called with Camera.takePicture(shutter,raw, jpeg)
        //also it seems Camera.takePicture is nonfunctional/crash on a6000
        //activityInterface.getCamera().setShutterListener(this);

        //m_camera.setJpegListener(); maybe is used to get jpeg/raw data returned


        CameraInstance.GET().setFocusDriveListener(this);

        m_viewFlags = Preferences.GET().getViewFlags(VIEW_FLAG_GRID | VIEW_FLAG_HISTOGRAM);
        activityInterface.getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                setDialMode(Preferences.GET().getDialMode(0));
            }
        });


        updateViewVisibility();

        Log.d(TAG,"initUiEnd");
        timeLog.logTime();
    }

    /*private Runnable addExposureModeRunnable = new Runnable() {
        @Override
        public void run() {
            exposureMode = new ExposureModeView(getContext());
            exposureMode.setOnClickListener(CameraUiFragment.this);
            exposureMode.setActivity(activityInterface);
            dialViews.add(exposureMode);
            leftHolder.addView(exposureMode);
            exposureMode.updateImage();
        }
    };*/

   /* private Runnable addDriveModeRunnable = new Runnable() {
        @Override
        public void run() {
            driveMode = new DriveMode(getContext());
            driveMode.setOnClickListener(CameraUiFragment.this);
            driveMode.setActivity(activityInterface);
            dialViews.add(driveMode);
            leftHolder.addView(driveMode);
            driveMode.updateImage();
        }
    };*/

    /*private Runnable addImageStabRunnable = new Runnable() {
        @Override
        public void run() {
            imageStabView = new ImageStabView(getContext());
            imageStabView.setActivity(activityInterface);
            imageStabView.setOnClickListener(CameraUiFragment.this);
            dialViews.add(imageStabView);
            leftHolder.addView(imageStabView);
            imageStabView.updateImage();
        }
    };*/
/*
    private Runnable addLongExpoNoiseReductionRunnable = new Runnable() {
        @Override
        public void run() {
            longExpoNR = new LongExpoNR(getContext());
            longExpoNR.setActivity(activityInterface);
            longExpoNR.setOnClickListener(CameraUiFragment.this);
            dialViews.add(longExpoNR);
            leftHolder.addView(longExpoNR);
            longExpoNR.updateImage();
        }
    };*/

   /* private Runnable addTimelapseRunnable = new Runnable() {
        @Override
        public void run() {
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
                    return activityInterface.getResString(R.string.view_startBracket_Timelapse);
                }
            };
            //noinspection ResourceType
            m_ivTimelapse.setImageResource(getResources().getInteger(R.integer.p_16_dd_parts_43_shoot_icon_setting_drivemode_invalid));
            m_ivTimelapse.setOnClickListener(CameraUiFragment.this);
            dialViews.add(m_ivTimelapse);
            leftHolder.addView(m_ivTimelapse);
        }
    };

    private Runnable addBracketRunnable = new Runnable() {
        @Override
        public void run() {
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
                    return activityInterface.getResString(R.string.view_startBracket_Timelapse);
                }
            };
            //noinspection ResourceType
            m_ivBracket.setImageResource(getResources().getInteger(R.integer.p_16_dd_parts_contshot));
            m_ivBracket.setOnClickListener(CameraUiFragment.this);
            dialViews.add(m_ivBracket);
            leftHolder.addView(m_ivBracket);
            // Shutter
            ShutterController.GetInstance().setShutterSpeedEventListner(bracket);
        }
    };

    private Runnable addBottomItems = new Runnable() {
        @Override
        public void run() {
            final int margineright = (int)getResources().getDimension(R.dimen.bottomHolderChildMarginRight);
            LinearLayout.LayoutParams params= new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, margineright, 0);
            params.weight = 1;


            //dialViews.add(evCompensation);

            setDialMode(0);
        }
    };*/

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
                if (m_tvHint.getVisibility() != VISIBLE)
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

    /*@Override
    public ExposureModeView getExposureMode() {
        return exposureMode;
    }*/

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
    public void setLeftViewVisibility(boolean visible)
    {
        final int visibility = visible ? View.VISIBLE : View.GONE;
        leftHolder.setVisibility(visibility);
        bottomHolder.setVisibility(visibility);
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
        /*if(view instanceof BaseImageView)
            ((BaseImageView) view).toggle();
        else
        if (view.equals(timelapse))
            timelapse.prepare();
        else if (view.equals(bracket))
            bracket.prepare();*/
    }

    private void setDialMode(final int mode)
    {
        Log.d(TAG , "setDialMode:" +mode);
        Controller lastView = dialViews.get(lastDialView);
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
        try {
            showHintMessage(getResources().getString(lastView.getNavigationHelpID()));
        }
        catch (Resources.NotFoundException ex)
        {
            ex.printStackTrace();
        }

    }




    /*  ##################################################################
        ## Key events impl ##
        ##################### */

    @Override
    public boolean onUpperDialChanged(int value)
    {
        dialViews.get(lastDialView).setValue(value);

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
        Controller view = dialViews.get(lastDialView);
        view.toggle();
        Log.d(TAG,"onEnterKeyDown");
       /* if (view instanceof BaseImageView)
            ((BaseImageView) view).toggle();
        else if (view instanceof BaseTextView)
            ((BaseTextView) view).onClick();*/
       try {
           showHintMessage(getResources().getString(view.getNavigationHelpID()));
       }
       catch (Resources.NotFoundException ex)
       {
           ex.printStackTrace();
       }

        return true;
    }

    @Override
    public boolean onFnKeyDown() {
        return false;
    }

    @Override
    public boolean onFnKeyUp() {
        CameraInstance.GET().cancelCapture();

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
        setDialMode(1);
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
        return true;
    }

    @Override
    public boolean onShutterKeyDown()
    {
        Log.d(TAG,"onShutterKeyDown");
        return true;
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
    public void onAnalizedData(CameraEx.AnalizedData analizedData, CameraEx cameraEx) {
        if (analizedData != null && analizedData.hist != null && analizedData.hist.Y != null && m_vHist.getVisibility() == View.VISIBLE)
            m_vHist.setHistogram(analizedData.hist.Y);
    }
    //############### CameraEx.PreviewAnalizeListener END###############



    //##########CameraEx.FocusDriveListner################
    @Override
    public void onChanged(CameraEx.FocusPosition focusPosition, CameraEx cameraEx) {

            m_lFocusScale.setVisibility(View.VISIBLE);
            m_focusScaleView.setMaxPosition(focusPosition.maxPosition);
            m_focusScaleView.setCurPosition(focusPosition.currentPosition);
            activityInterface.getMainHandler().removeCallbacks(m_hideFocusScaleRunnable);
            activityInterface.getMainHandler().postDelayed(m_hideFocusScaleRunnable, 2000);

    }
    //##########CameraEx.FocusDriveListner ENDs################

}
