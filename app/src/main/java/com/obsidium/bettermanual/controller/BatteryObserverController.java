package com.obsidium.bettermanual.controller;



public class BatteryObserverController extends TextViewController{


    private static BatteryObserverController batteryObserverController = new BatteryObserverController();

    public static BatteryObserverController GetInstance() {
        return batteryObserverController;
    }

    @Override
    public void toggle() {

    }

    @Override
    public int getNavigationHelpID() {
        return 0;
    }
}
