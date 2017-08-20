package com.obsidium.bettermanual.views;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;

/**
 * Created by KillerInk on 22.08.2017.
 */

public class EvView extends BaseTextView {

    private int             m_maxExposureCompensation;
    private int             m_minExposureCompensation;
    private int             m_curExposureCompensation;
    private float           m_exposureCompensationStep;

    public EvView(Context context) {
        super(context);
    }

    public EvView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EvView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onScrolled(int distance) {
        if (activityInterface.getIso().getCurrentIso() != 0) {
            if (distance > 0)
                decrementExposureCompensation(true);
            else
                incrementExposureCompensation(true);
        }
    }

    @Override
    public boolean onClick() {
        // Reset exposure compensation
        setExposureCompensation(0);
        return true;
    }

    public void init(int max, int min, float step, int current)
    {
        m_maxExposureCompensation = max;
        m_minExposureCompensation = min;
        m_exposureCompensationStep = step;
        m_curExposureCompensation =current;
        updateExposureCompensation(false);
    }

    private void setExposureCompensation(int value)
    {
        m_curExposureCompensation = value;
        Camera.Parameters params = activityInterface.getCamera().createEmptyParameters();
        params.setExposureCompensation(value);
        activityInterface.getCamera().getNormalCamera().setParameters(params);
        updateExposureCompensation(false);
    }

    private void decrementExposureCompensation(boolean notify)
    {
        if (m_curExposureCompensation > m_minExposureCompensation)
        {
            --m_curExposureCompensation;

            Camera.Parameters params = activityInterface.getCamera().createEmptyParameters();
            params.setExposureCompensation(m_curExposureCompensation);
            activityInterface.getCamera().getNormalCamera().setParameters(params);

            updateExposureCompensation(notify);
        }
    }

    private void incrementExposureCompensation(boolean notify)
    {
        if (m_curExposureCompensation < m_maxExposureCompensation)
        {
            ++m_curExposureCompensation;

            Camera.Parameters params = activityInterface.getCamera().createEmptyParameters();
            params.setExposureCompensation(m_curExposureCompensation);
            activityInterface.getCamera().getNormalCamera().setParameters(params);

            updateExposureCompensation(notify);
        }
    }

    private void updateExposureCompensation(boolean notify)
    {
        final String text;
        if (m_curExposureCompensation == 0)
            text = "\uEB18\u00B10.0";
        else if (m_curExposureCompensation > 0)
            text = String.format("\uEB18+%.1f", m_curExposureCompensation * m_exposureCompensationStep);
        else
            text = String.format("\uEB18%.1f", m_curExposureCompensation * m_exposureCompensationStep);
        setText(text);
        if (notify)
            activityInterface.showMessageDelayed(text);
    }
}
