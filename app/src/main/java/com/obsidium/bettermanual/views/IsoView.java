package com.obsidium.bettermanual.views;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;

import com.sony.scalar.hardware.CameraEx;

import java.util.List;

/**
 * Created by KillerInk on 22.08.2017.
 */

public class IsoView extends BaseTextView implements CameraEx.AutoISOSensitivityListener {

    // ISO
    private int m_curIso;
    private List<Integer> m_supportedIsos;

    public IsoView(Context context) {
        super(context);
    }

    public IsoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IsoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void init(List<Integer> m_supportedIsos, int curIso)
    {
        this.m_supportedIsos = m_supportedIsos;
        m_curIso = curIso;
        setText(String.format("\uE488 %d", m_curIso));
    }

    public int getCurrentIso()
    {
        return m_curIso;
    }

    @Override
    public void onScrolled(int distance) {
        if (m_curIso != 0) {
            final int iso = distance < 0 ? getPreviousIso(m_curIso) : getNextIso(m_curIso);
            if (iso != 0) {
                setIso(iso);
                activityInterface.showMessageDelayed(String.format("\uE488 %d", iso));
            }
        }
    }

    @Override
    public boolean onClick() {
        setIso(m_curIso == 0 ? getFirstManualIso() : 0);
        activityInterface.showMessageDelayed(m_curIso == 0 ? "Auto \uE488" : "Manual \uE488");
        return false;
    }

    private void setIso(int iso) {
        //log("setIso: " + String.valueOf(iso) + "\n");
        m_curIso = iso;
        setText(String.format("\uE488 %s", (iso == 0 ? "AUTO" : String.valueOf(iso))));
        Camera.Parameters params = activityInterface.getCamera().createEmptyParameters();
        activityInterface.getCamera().createParametersModifier(params).setISOSensitivity(iso);
        activityInterface.getCamera().getNormalCamera().setParameters(params);
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

    private int getFirstManualIso() {
        for (Integer iso : m_supportedIsos) {
            if (iso != 0)
                return iso;
        }
        return 0;
    }

    @Override
    public void onChanged(int i, CameraEx cameraEx) {
        setText("\uE488 " + String.valueOf(i) + (m_curIso == 0 ? "(A)" : ""));
    }

    @Override
    public void setIn_DecrementValue(int value) {
        onScrolled(value);
    }
}