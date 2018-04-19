package com.obsidium.bettermanual.model;

import com.obsidium.bettermanual.camera.CameraInstance;
import com.sony.scalar.hardware.CameraEx;

public class HistogramModel extends AbstractModel<short[]> implements CameraEx.PreviewAnalizeListener {

    private short[] y;
    public HistogramModel(CameraInstance camera) {
        super(camera);
    }

    @Override
    public void setValue(int i) {

    }

    @Override
    public short[] getValue() {
        return y;
    }

    @Override
    public boolean isSupported() {
        return false;
    }

    @Override
    public void onAnalizedData(CameraEx.AnalizedData analizedData, CameraEx cameraEx) {
        if (analizedData != null && analizedData.hist != null && analizedData.hist.Y != null)
        {
            y = analizedData.hist.Y;
            fireOnValueChanged();
        }
    }
}
