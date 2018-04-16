package com.obsidium.bettermanual.model;

import com.obsidium.bettermanual.camera.CameraInstance;
import com.sony.scalar.hardware.CameraEx;

public class ExposureHintModel extends AbstractModel<String> implements CameraEx.ProgramLineRangeOverListener {
    public ExposureHintModel(CameraInstance camera) {
        super(camera);
    }

    @Override
    public void setValue(int i) {

    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean isSupported() {
        return true;
    }

    @Override
    public void onAERange(boolean b, boolean b1, boolean b2, CameraEx cameraEx) {

    }

    @Override
    public void onEVRange(int ev, CameraEx cameraEx) {
        final String text;
        if (ev == 0)
            value = "\u00B10.0";
        else if (ev > 0)
            value = String.format("+%.1f", (float)ev / 3.0f);
        else
            value = String.format("%.1f", (float)ev / 3.0f);
        fireOnValueChanged();
    }

    @Override
    public void onMeteringRange(boolean b, CameraEx cameraEx) {

    }
}
