package com.obsidium.bettermanual.controller;

import com.obsidium.bettermanual.model.Model;
import com.obsidium.bettermanual.model.ShutterModel;
import com.sony.scalar.hardware.CameraEx;

public class ShutterController extends TextViewController<ShutterModel> {

    public interface ShutterSpeedEvent
    {
        void onChanged();
    }

    private static ShutterController shutterController = new ShutterController();

    public static ShutterController GetInstance()
    {
        return shutterController;
    }

    private ShutterSpeedEvent shutterSpeedEventListner;


    @Override
    public void toggle() {

    }

    @Override
    public int getNavigationHelpID() {
        return 0;
    }

    public CameraEx.ShutterSpeedInfo getShutterSpeedInfo()
    {
        if (model != null)
            return model.getShutterSpeedInfo();
        return null;
    }

    public void setShutterSpeedEventListner(ShutterSpeedEvent eventListner)
    {
        this.shutterSpeedEventListner = eventListner;
    }

    @Override
    public void onValueChanged() {
        super.onValueChanged();
        if (shutterSpeedEventListner != null)
            shutterSpeedEventListner.onChanged();
    }
}
