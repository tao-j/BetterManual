package com.obsidium.bettermanual.controller;

import android.view.View;
import android.widget.TextView;

import com.obsidium.bettermanual.R;
import com.obsidium.bettermanual.model.Model;

public class ApertureController extends TextViewController<Model<String>> {

    private static ApertureController apertureController = new ApertureController();

    public static ApertureController GetInstance()
    {
        return apertureController;
    }


    @Override
    public void bindView(TextView v) {
        super.bindView(v);
        setText();
    }

    @Override
    public void toggle() {

    }

    @Override
    public int getNavigationHelpID() {
        return R.string.view_aperture_navigation;
    }

    @Override
    public void onValueChanged() {
        setText();
    }

    @Override
    protected void setText()
    {
        if (model!= null && view != null){
            view.setText(model.getValue());
        }
    }
}
