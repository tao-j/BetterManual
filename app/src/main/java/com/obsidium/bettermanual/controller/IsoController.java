package com.obsidium.bettermanual.controller;

import com.obsidium.bettermanual.model.IsoModel;
import com.obsidium.bettermanual.model.Model;

public class IsoController extends ApertureController {

    private static IsoController isoController = new IsoController();

    private IsoModel isoModel;

    @Override
    public void bindModel(Model model) {
        super.bindModel(model);
        isoModel = (IsoModel) model;
    }

    public static IsoController GetInstance() {
        return isoController;
    }

    public int getCurrentIso()
    {
        if (isoModel != null)
            return isoModel.getCurrentIso();
        return 0;
    }
}
