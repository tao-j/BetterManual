package com.obsidium.bettermanual.controller;

public class ExposureCompensationController extends ApertureController {
    private static ExposureCompensationController exposureCompensationController = new ExposureCompensationController();

    public static ExposureCompensationController GetInstance()
    {
        return exposureCompensationController;
    }
}
