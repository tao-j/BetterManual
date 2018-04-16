package com.obsidium.bettermanual.controller;

import com.obsidium.bettermanual.model.ExposureHintModel;

public class ExposureHintController extends TextViewController<ExposureHintModel> {
    private static ExposureHintController exposureHintController =new ExposureHintController();

    public static ExposureHintController GetInstance()
    {
        return exposureHintController;
    }

    @Override
    public void toggle() {

    }

    @Override
    public int getNavigationHelpID() {
        return 0;
    }
}
