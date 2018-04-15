package com.obsidium.bettermanual.controller;

import android.view.View;
import android.widget.TextView;

public class ApertureController extends AbstractController {

    private static ApertureController apertureController = new ApertureController();

    public static ApertureController GetInstance()
    {
        return apertureController;
    }

    @Override
    public void bindView(View v) {
        super.bindView(v);
        setText();
    }

    @Override
    public void onValueChanged() {
        setText();
    }

    private void setText()
    {
        if (model!= null)
            ((TextView)view).setText(model.getValue());
    }
}
