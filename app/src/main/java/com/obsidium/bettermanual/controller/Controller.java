package com.obsidium.bettermanual.controller;

import android.view.View;

import com.obsidium.bettermanual.model.Model;

public interface Controller<T extends View> {
    void bindView(T v);
    void bindModel(Model model);
    void setValue(int i);
}
