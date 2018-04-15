package com.obsidium.bettermanual.controller;

import com.obsidium.bettermanual.model.Model;
import com.obsidium.bettermanual.model.ShutterModel;
import com.sony.scalar.hardware.CameraEx;

public class ShutterController extends ApertureController {

    public interface ShutterSpeedEvent
    {
        void onChanged();
    }

    private static ShutterController shutterController = new ShutterController();

    public static ShutterController GetInstance()
    {
        return shutterController;
    }

    private ShutterModel shutterModel;
    private ShutterSpeedEvent shutterSpeedEventListner;

    @Override
    public void bindModel(Model model) {
        super.bindModel(model);
        this.shutterModel = (ShutterModel)model;
    }

    public CameraEx.ShutterSpeedInfo getShutterSpeedInfo()
    {
        if (shutterModel != null)
            return shutterModel.getShutterSpeedInfo();
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
