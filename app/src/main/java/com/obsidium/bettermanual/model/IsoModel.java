package com.obsidium.bettermanual.model;

import com.obsidium.bettermanual.camera.CameraInstance;
import com.sony.scalar.hardware.CameraEx;

import java.util.List;

public class IsoModel extends AbstractModel<String> implements CameraEx.AutoISOSensitivityListener {


    private int m_curIso;
    private List<Integer> m_supportedIsos;

    public IsoModel(CameraInstance cameraInstance)
    {
        super(cameraInstance);
        this.m_supportedIsos = camera.getSupportedISOSensitivities();
        setValue(camera.getISOSensitivity());
        value = "\uE488 " + String.valueOf(0) + (m_curIso == 0 ? "(A)" : "");
        fireOnValueChanged();

    }

    @Override
    public void setValue(int i) {
        final int iso = i < 0 ? getPreviousIso(m_curIso) : getNextIso(m_curIso);
        m_curIso = iso;
        value = String.format("\uE488 %s", (iso == 0 ? "AUTO" : String.valueOf(iso)));

        camera.setISOSensitivity(iso);
        fireOnValueChanged();
    }

    public void toggle()
    {
        m_curIso = getCurrentIso() == 0 ? getFirstManualIso() : 0;
        value = String.format("\uE488 %s", (m_curIso == 0 ? "AUTO" : String.valueOf(m_curIso)));

        camera.setISOSensitivity(m_curIso);
        fireOnValueChanged();
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
        fireOnValueChanged();
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

    public int getFirstManualIso() {
        if (m_supportedIsos == null)
            return 0;
        for (Integer iso : m_supportedIsos) {
            if (iso != 0)
                return iso;
        }
        return 0;
    }
}
