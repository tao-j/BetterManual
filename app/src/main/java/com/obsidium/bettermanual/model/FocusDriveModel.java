package com.obsidium.bettermanual.model;

import com.obsidium.bettermanual.camera.CameraInstance;
import com.sony.scalar.hardware.CameraEx;

public class FocusDriveModel extends AbstractModel<Integer> implements CameraEx.FocusDriveListener {

    private int currentPosition;
    private int maxPosition;

    public FocusDriveModel(CameraInstance camera) {
        super(camera);
    }

    @Override
    public void setValue(int i) {
        camera.setFocusPosition(i);
    }

    @Override
    public Integer getValue() {
        return currentPosition;
    }

    public int getMaxPosition()
    {
        return maxPosition;
    }

    @Override
    public boolean isSupported() {
        return false;
    }

    @Override
    public void onChanged(CameraEx.FocusPosition focusPosition, CameraEx cameraEx) {
        if (focusPosition != null)
        {
            currentPosition = focusPosition.currentPosition;
            maxPosition = focusPosition.maxPosition;
            fireOnValueChanged();
        }
    }
}
