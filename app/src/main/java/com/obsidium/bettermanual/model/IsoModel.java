package com.obsidium.bettermanual.model;

import com.obsidium.bettermanual.camera.CameraInstance;
import com.obsidium.bettermanual.controller.IsoController;
import com.sony.scalar.hardware.CameraEx;

import java.util.List;

public class IsoModel extends AbstractModel implements CameraEx.AutoISOSensitivityListener {


    private int m_curIso;
    private List<Integer> m_supportedIsos;

    public IsoModel()
    {
        this.m_supportedIsos = CameraInstance.GET().getSupportedISOSensitivities();
        IsoController.GetInstance().bindModel(this);
    }

    @Override
    public void setValue(int i) {
        if (m_curIso != 0) {
            m_curIso = i < 0 ? getPreviousIso(m_curIso) : getNextIso(m_curIso);
            if (m_curIso != 0) {
                CameraInstance.GET().setISOSensitivity(m_curIso);
                value = String.format("\uE488 %d", m_curIso);
                if (eventsListner != null)
                    eventsListner.onValueChanged();

            }
        }
    }

    @Override
    public String getValue() {
        return value;
    }


    @Override
    public boolean isSupported() {
        return true;
    }

    //AutoIsoSensitivityListner
    @Override
    public void onChanged(int i, CameraEx cameraEx) {
        value = "\uE488 " + String.valueOf(i) + (m_curIso == 0 ? "(A)" : "");
        if (eventsListner != null)
            eventsListner.onValueChanged();
    }

    private int getPreviousIso(int current) {
        int previous = 0;
        for (Integer iso : m_supportedIsos) {
            if (iso == current)
                return previous;
            else
                previous = iso;
        }
        return 0;
    }

    private int getNextIso(int current) {
        boolean next = false;
        for (Integer iso : m_supportedIsos) {
            if (next)
                return iso;
            else if (iso == current)
                next = true;
        }
        return current;
    }

    public int getCurrentIso()
    {
        return m_curIso;
    }
}
