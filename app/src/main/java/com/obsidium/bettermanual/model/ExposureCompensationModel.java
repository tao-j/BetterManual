package com.obsidium.bettermanual.model;

import com.obsidium.bettermanual.camera.CameraInstance;

public class ExposureCompensationModel extends AbstractModel<String> {

    private int             m_maxExposureCompensation;
    private int             m_minExposureCompensation;
    private int             m_curExposureCompensation;
    private float           m_exposureCompensationStep;


    public ExposureCompensationModel(CameraInstance cameraInstance)
    {
        super(cameraInstance);
        m_maxExposureCompensation = camera.getMaxExposureCompensation();
        m_minExposureCompensation = camera.getMinExposureCompensation();
        m_exposureCompensationStep = camera.getExposureCompensationStep();
        m_curExposureCompensation = camera.getExposureCompensation();
        setEvValue();
        fireOnValueChanged();
    }

    @Override
    public void setValue(int i) {
        if (i > 0)
            decrementExposureCompensation();
        else
            incrementExposureCompensation();
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean isSupported() {
        return true;
    }

    private void decrementExposureCompensation()
    {
        if (m_curExposureCompensation > m_minExposureCompensation)
        {
            --m_curExposureCompensation;
            camera.setExposureCompensation(m_curExposureCompensation);
            setEvValue();
            fireOnValueChanged();
        }
    }

    private void incrementExposureCompensation()
    {
        if (m_curExposureCompensation < m_maxExposureCompensation)
        {
            ++m_curExposureCompensation;
            camera.setExposureCompensation(m_curExposureCompensation);
            setEvValue();
            fireOnValueChanged();
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
