package com.obsidium.bettermanual.controller;

import android.view.View;
import android.widget.TextView;

import com.obsidium.bettermanual.model.Model;

public abstract class AbstractController implements Controller, Model.Events {


    protected View view;
    protected Model model;

    @Override
    public void bindView(View v) {
        this.view = v;
    }

    @Override
    public void bindModel(Model model) {
        this.model = model;
        model.setListner(this);
    }

    @Override
    public void onIsSupportedChanged() {
        if (model != null){
            if (view != null)
                view.setVisibility(model.isSupported() ? View.VISIBLE : View.INVISIBLE);
        }
        else if (view != null)
            view.setVisibility(View.GONE);
    }

    @Override
    public void setValue(int i) {
        if (model != null)
            model.setValue(i);
    }
}
