package com.obsidium.bettermanual.model;

import com.obsidium.bettermanual.camera.CameraInstance;

public class LongExposureNoiseReductionModel extends AbstractModel<Boolean> {

    public LongExposureNoiseReductionModel(CameraInstance camera) {
        super(camera);
    }

    @Override
    public void setValue(int i) {
        if (camera.getLongeExposureNR()) {
            camera.setLongExposureNoiseReduction(false);
        }
        else
            camera.setLongExposureNoiseReduction(true);
        fireOnValueChanged();
    }

    @Override
    public Boolean getValue() {
        return camera.getLongeExposureNR();
    }

    @Override
    public boolean isSupported() {
        return true;
    }
}
