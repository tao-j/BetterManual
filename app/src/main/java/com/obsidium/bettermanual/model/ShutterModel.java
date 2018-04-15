package com.obsidium.bettermanual.model;

import android.util.Pair;

import com.obsidium.bettermanual.CameraUtil;
import com.obsidium.bettermanual.camera.CameraInstance;
import com.obsidium.bettermanual.controller.ShutterController;
import com.sony.scalar.hardware.CameraEx;

public class ShutterModel extends AbstractModel implements CameraEx.ShutterSpeedChangeListener {

    public ShutterModel()
    {
        ShutterController.GetInstance().bindModel(this);
    }

    private CameraEx.ShutterSpeedInfo shutterSpeedInfo;

    @Override
    public void setValue(int i) {
        if (i > 0)
            CameraInstance.GET().decrementShutterSpeed();
        else
            CameraInstance.GET().incrementShutterSpeed();
    }

    @Override
    public String getValue() {
        Pair<Integer,Integer> s =  CameraInstance.GET().getShutterSpeed();
        value = CameraUtil.formatShutterSpeed(s.first, s.second);
        return value;
    }

    @Override
    public boolean isSupported() {
        return true;
    }

    @Override
    public void onShutterSpeedChange(CameraEx.ShutterSpeedInfo shutterSpeedInfo, CameraEx cameraEx) {
        this.shutterSpeedInfo = shutterSpeedInfo;
        value = CameraUtil.formatShutterSpeed(shutterSpeedInfo.currentShutterSpeed_n, shutterSpeedInfo.currentShutterSpeed_d);
        if (eventsListner != null)
            eventsListner.onValueChanged();
    }

    public CameraEx.ShutterSpeedInfo getShutterSpeedInfo()
    {
        return shutterSpeedInfo;
    }
}
