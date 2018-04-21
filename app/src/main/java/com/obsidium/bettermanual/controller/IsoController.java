package com.obsidium.bettermanual.controller;

import android.widget.TextView;

import com.obsidium.bettermanual.R;
import com.obsidium.bettermanual.model.IsoModel;
import com.obsidium.bettermanual.model.Model;

public class IsoController extends TextViewController<IsoModel>{

    private static IsoController isoController = new IsoController();


    @Override
    public void toggle() {
        model.toggle();
    }

    public static IsoController GetInstance() {
        return isoController;
    }

    @Override
    public int getNavigationHelpID() {
        if (model != null && model.getCurrentIso() == 0)
            return R.string.view_iso_navigation_auto;
        return R.string.view_iso_navigation_manual;
    }

    public int getCurrentIso()
    {
        if (model != null)
            return model.getCurrentIso();
        return 0;
    }


}
