package com.obsidium.bettermanual.views;

import android.content.Context;
import android.util.AttributeSet;

import com.github.killerink.camera.CameraInstance;
import com.obsidium.bettermanual.R;

/**
 * Created by KillerInk on 22.08.2017.
 */

public class EvView extends BaseTextView {

    @Override
    public void setIn_DecrementValue(int value) {
        onScrolled(value);
    }

    @Override
    public String getNavigationString() {
        return null;
    }

    private int             m_maxExposureCompensation;
    private int             m_minExposureCompensation;
    private int             m_curExposureCompensation;
    private float           m_exposureCompensationStep;
    private CameraInstance wrapper;

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
        if (distance > 0)
            decrementExposureCompensation(true);
        else
            incrementExposureCompensation(true);
    }

    @Override
    public boolean onClick() {
        // Reset exposure compensation
        setExposureCompensation(0);
        return true;
    }

    public void init(CameraInstance wrapper)
    {
        this.wrapper = wrapper;
        m_maxExposureCompensation = wrapper.getMaxExposureCompensation();
        m_minExposureCompensation = wrapper.getMinExposureCompensation();
        m_exposureCompensationStep = wrapper.getExposureCompensationStep();
        m_curExposureCompensation = wrapper.getExposureCompensation();
        updateExposureCompensation(false);
    }

    private void setExposureCompensation(int value)
    {
        wrapper.setExposureCompensation(value);
        m_curExposureCompensation = value;
        updateExposureCompensation(false);
    }

    private void decrementExposureCompensation(boolean notify)
    {
        if (m_curExposureCompensation > m_minExposureCompensation)
        {
            --m_curExposureCompensation;
            wrapper.setExposureCompensation(m_curExposureCompensation);
            updateExposureCompensation(notify);
        }
    }

    private void incrementExposureCompensation(boolean notify)
    {
        if (m_curExposureCompensation < m_maxExposureCompensation)
        {
            ++m_curExposureCompensation;
            wrapper.setExposureCompensation(m_curExposureCompensation);
            updateExposureCompensation(notify);
        }
    }

    private void updateExposureCompensation(final boolean notify)
    {
        final String text;
        if (m_curExposureCompensation == 0)
            text = "\uEB18\u00B10.0";
        else if (m_curExposureCompensation > 0)
            text = String.format(cameraUiInterface.getActivityInterface().getResString(R.string.icon_ev) + "+%.1f", m_curExposureCompensation * m_exposureCompensationStep);
        else
            text = String.format(cameraUiInterface.getActivityInterface().getResString(R.string.icon_ev) + "%.1f", m_curExposureCompensation * m_exposureCompensationStep);
        setText(text);
        if (notify)
            cameraUiInterface.showMessageDelayed(text);
    }
}
