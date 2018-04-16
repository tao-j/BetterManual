package com.obsidium.bettermanual.model;

import com.obsidium.bettermanual.camera.CameraInstance;
import com.sony.scalar.hardware.CameraEx;

public class ApertureModel extends AbstractModel<String> implements CameraEx.ApertureChangeListener {

    public ApertureModel(CameraInstance camera) {
        super(camera);
    }

    @Override
    public void setValue(int i) {
        if (i > 0)
            camera.decrementAperture();
        else
            camera.incrementAperture();
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean isSupported() {
        return supported;
    }

    @Override
    public void onApertureChange(CameraEx.ApertureInfo apertureInfo, CameraEx cameraEx) {
        supported = apertureInfo.currentAperture != 0;
        value =  String.format("f%.1f", (float)apertureInfo.currentAperture / 100.0f);
        fireOnValueChanged();
        fireOnSupportedChanged();
    }
}
