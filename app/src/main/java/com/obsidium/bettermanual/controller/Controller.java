package com.obsidium.bettermanual.controller;

import android.view.View;

import com.obsidium.bettermanual.model.Model;

public interface Controller<V extends View,M extends Model> {
    void bindView(V v);
    void bindModel(M model);
    void setValue(int i);
    void toggle();
    void setColorToView(Integer color);
    int getNavigationHelpID();
}
