package com.obsidium.bettermanual.model;

import com.obsidium.bettermanual.camera.CameraHandler;
import com.obsidium.bettermanual.camera.CameraInstance;
import com.obsidium.bettermanual.controller.ApertureController;
import com.sony.scalar.hardware.CameraEx;

public class ApertureModel extends AbstractModel implements CameraEx.ApertureChangeListener {

    public void ApertureModel()
    {
        ApertureController.GetInstance().bindModel(this);
    }

    @Override
    public void setValue(int i) {
        if (i > 0)
            CameraInstance.GET().decrementAperture();
        else
            CameraInstance.GET().incrementAperture();
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
        if (eventsListner != null)
            eventsListner.onIsSupportedChanged();
        value =  String.format("f%.1f", (float)apertureInfo.currentAperture / 100.0f);
        if (eventsListner != null)
            eventsListner.onValueChanged();
    }
}
