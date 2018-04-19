package com.obsidium.bettermanual.controller;

import android.view.View;

import com.obsidium.bettermanual.model.HistogramModel;
import com.obsidium.bettermanual.views.HistogramView;

public class HistogramController extends AbstractController<HistogramView,HistogramModel> {


    private static HistogramController histogramController = new HistogramController();

    public static HistogramController GetInstance()
    { return histogramController;}

    @Override
    public void toggle() {

    }

    @Override
    public void setColorToView(Integer color) {

    }

    @Override
    public int getNavigationHelpID() {
        return 0;
    }

    @Override
    public void onValueChanged() {
        if (view != null && view.getVisibility() == View.VISIBLE && model != null && model.getValue() != null)
            view.setHistogram(model.getValue());
    }

    public void setVisibility(boolean visibile)
    {
        if (view != null)
        {
            view.setVisibility(visibile ? View.VISIBLE : View.GONE);
        }
    }
}
