package com.obsidium.bettermanual.model;

import com.obsidium.bettermanual.R;
import com.obsidium.bettermanual.camera.CameraInstance;
import com.obsidium.bettermanual.controller.ExposureCompensationController;

public class ExposureCompensationModel extends AbstractModel {

    private int             m_maxExposureCompensation;
    private int             m_minExposureCompensation;
    private int             m_curExposureCompensation;
    private float           m_exposureCompensationStep;


    public ExposureCompensationModel()
    {
        m_maxExposureCompensation = CameraInstance.GET().getMaxExposureCompensation();
        m_minExposureCompensation = CameraInstance.GET().getMinExposureCompensation();
        m_exposureCompensationStep = CameraInstance.GET().getExposureCompensationStep();
        m_curExposureCompensation = CameraInstance.GET().getExposureCompensation();
        ExposureCompensationController.GetInstance().bindModel(this);
    }

    @Override
    public void setValue(int i) {
        if (i > 0)
            decrementExposureCompensation(true);
        else
            incrementExposureCompensation(true);
    }

    @Override
    public String getValue() {
        return null;
    }

    @Override
    public boolean isSupported() {
        return false;
    }

    private void decrementExposureCompensation(boolean notify)
    {
        if (m_curExposureCompensation > m_minExposureCompensation)
        {
            --m_curExposureCompensation;
            CameraInstance.GET().setExposureCompensation(m_curExposureCompensation);
            setEvValue();
            if (eventsListner != null)
                eventsListner.onValueChanged();
        }
    }

    private void incrementExposureCompensation(boolean notify)
    {
        if (m_curExposureCompensation < m_maxExposureCompensation)
        {
            ++m_curExposureCompensation;
            CameraInstance.GET().setExposureCompensation(m_curExposureCompensation);
            setEvValue();
            if (eventsListner != null)
                eventsListner.onValueChanged();
        }
    }

    private void setEvValue()
    {
        final String text;
        if (m_curExposureCompensation == 0)
            value = "\uEB18\u00B10.0";
        else
            value = String.format("+%.1f", m_curExposureCompensation * m_exposureCompensationStep);
    }
}
